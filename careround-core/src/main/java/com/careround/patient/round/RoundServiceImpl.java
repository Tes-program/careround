package com.careround.patient.round;

import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientRoundReview;
import com.careround.patient.entity.Round;
import com.careround.patient.enums.AssignedToRole;
import com.careround.patient.enums.ClinicalStatus;
import com.careround.patient.enums.DischargeAssessment;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskSource;
import com.careround.auth.enums.UserRole;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientRoundReviewRepository;
import com.careround.patient.repository.RoundRepository;
import com.careround.patient.round.dto.CreateRoundRequest;
import com.careround.patient.round.dto.PatientRoundReviewResponse;
import com.careround.patient.round.dto.ReviewPatientRequest;
import com.careround.patient.round.dto.RoundResponse;
import com.careround.shared.event.PatientDischargeReadyEvent;
import com.careround.shared.event.RoundCompletedEvent;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final PatientRoundReviewRepository patientRoundReviewRepository;
    private final WardRepository wardRepository;
    private final MedicalTeamRepository medicalTeamRepository;
    private final ShiftRepository shiftRepository;
    private final PatientRepository patientRepository;
    private final CareTaskRepository careTaskRepository;
    private final OutboxService outboxService;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public RoundResponse createRound(CreateRoundRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Ward ward = wardRepository.findById(request.wardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        if (!ward.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Ward does not belong to this hospital");

        MedicalTeam team = medicalTeamRepository.findById(request.medicalTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
        if (!team.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Medical team does not belong to this hospital");

        if (roundRepository.existsByWardIdAndMedicalTeamIdAndRoundTypeAndStatus(
                request.wardId(), request.medicalTeamId(), request.roundType(), RoundStatus.IN_PROGRESS))
            throw new BusinessRuleException("An active round of this type already exists for this ward and team");

        Shift shift = shiftRepository.findFirstByWardIdAndStatusOrderByStartTimeDesc(
                        request.wardId(), ShiftStatus.ACTIVE)
                .orElseThrow(() -> new BusinessRuleException("No active shift found for this ward"));

        Round round = new Round();
        round.setHospitalId(hospitalId);
        round.setWardId(request.wardId());
        round.setMedicalTeamId(request.medicalTeamId());
        round.setShiftId(shift.getId());
        round.setRoundType(request.roundType());
        round.setLeadDoctorId(request.leadDoctorId());
        round.setScheduledTime(request.scheduledTime());
        round.setTeamMembers(request.teamMembers());

        Round saved = roundRepository.save(round);
        log.info("action=createRound roundId={} hospitalId={} wardId={}", saved.getId(), hospitalId, saved.getWardId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RoundResponse startRound(String roundId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        if (!round.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Round does not belong to this hospital");
        if (round.getStatus() != RoundStatus.SCHEDULED)
            throw new BusinessRuleException("Only SCHEDULED rounds can be started");

        round.setStatus(RoundStatus.IN_PROGRESS);
        round.setStartedAt(LocalDateTime.now(ZoneOffset.UTC));

        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdAndStatusOrderByNewsScoreDescAdmissionDateAsc(
                        hospitalId, round.getWardId(), PatientStatus.ADMITTED);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<PatientRoundReview> reviews = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            PatientRoundReview review = new PatientRoundReview();
            review.setRoundId(roundId);
            review.setPatientId(patients.get(i).getId());
            review.setReviewedById(round.getLeadDoctorId());
            review.setReviewOrder(i + 1);
            review.setClinicalStatus(ClinicalStatus.STABLE);
            review.setDischargeAssessment(DischargeAssessment.NONE);
            review.setWasExamined(false);
            review.setNotifiedNextOfKin(false);
            review.setReviewedAt(now);
            reviews.add(review);
        }
        patientRoundReviewRepository.saveAll(reviews);

        log.info("action=startRound roundId={} hospitalId={} patientsQueued={}", roundId, hospitalId, patients.size());
        return toResponse(roundRepository.save(round));
    }

    @Override
    @Transactional
    public PatientRoundReviewResponse reviewPatient(String roundId, String patientId, ReviewPatientRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        if (!round.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Round does not belong to this hospital");
        if (round.getStatus() != RoundStatus.IN_PROGRESS)
            throw new BusinessRuleException("Round is not in progress");

        PatientRoundReview review = patientRoundReviewRepository.findByRoundIdAndPatientId(roundId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found in this round"));

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        if (request.dischargeAssessment() == DischargeAssessment.CONFIRMED
                && HospitalContextHolder.getRole() != UserRole.CONSULTANT) {
            throw new AccessDeniedException("Only consultants can confirm discharge");
        }

        review.setReviewedById(userId);
        review.setClinicalStatus(request.clinicalStatus());
        review.setWasExamined(request.wasExamined());
        review.setManagementPlan(request.managementPlan());
        review.setDischargeAssessment(request.dischargeAssessment());
        review.setNotifiedNextOfKin(request.notifiedNextOfKin());
        review.setNewsScoreAtReview(patient.getNewsScore());
        review.setReviewedAt(LocalDateTime.now(ZoneOffset.UTC));

        if (request.dischargeAssessment() == DischargeAssessment.CONFIRMED && !patient.isDischargeReady()) {
            markPatientDischargeReady(patient, round, userId, hospitalId);
        }

        log.info("action=reviewPatient roundId={} patientId={} userId={}", roundId, patientId, userId);
        return toReviewResponse(patientRoundReviewRepository.save(review));
    }

    @Override
    @Transactional
    public RoundResponse completeRound(String roundId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        if (!round.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Round does not belong to this hospital");
        if (round.getStatus() != RoundStatus.IN_PROGRESS)
            throw new BusinessRuleException("Only IN_PROGRESS rounds can be completed");

        round.setStatus(RoundStatus.COMPLETED);
        round.setCompletedAt(LocalDateTime.now(ZoneOffset.UTC));
        Round saved = roundRepository.save(round);
        Counter.builder("careround.round.completed")
                .tag("hospitalId", saved.getHospitalId())
                .register(meterRegistry)
                .increment();

        outboxService.publish("careround.round.completed",
                new RoundCompletedEvent(hospitalId, round.getId(), round.getWardId(),
                        round.getMedicalTeamId(), round.getShiftId(), round.getRoundType(),
                        round.getLeadDoctorId(), round.getCompletedAt(), MDC.get("correlationId")),
                hospitalId);

        log.info("action=completeRound roundId={} hospitalId={}", roundId, hospitalId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RoundResponse cancelRound(String roundId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        if (!round.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Round does not belong to this hospital");
        if (round.getStatus() != RoundStatus.SCHEDULED)
            throw new BusinessRuleException("Only SCHEDULED rounds can be cancelled");

        round.setStatus(RoundStatus.CANCELLED);
        Round saved = roundRepository.save(round);
        log.info("action=cancelRound roundId={} hospitalId={}", roundId, hospitalId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResponse> getRounds(String wardId, String teamId) {
        return roundRepository.findAllByWardIdAndMedicalTeamIdOrderByCreatedAtDesc(wardId, teamId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientRoundReviewResponse> getReviews(String roundId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        if (!round.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Round does not belong to this hospital");
        return patientRoundReviewRepository.findAllByRoundIdOrderByReviewOrder(roundId)
                .stream().map(this::toReviewResponse).toList();
    }

    private RoundResponse toResponse(Round r) {
        return new RoundResponse(r.getId(), r.getHospitalId(), r.getWardId(), r.getMedicalTeamId(),
                r.getShiftId(), r.getRoundType(), r.getLeadDoctorId(), r.getStatus(),
                r.getScheduledTime(), r.getStartedAt(), r.getCompletedAt(),
                r.getTeamMembers(), r.getCreatedAt(), r.getUpdatedAt());
    }

    private PatientRoundReviewResponse toReviewResponse(PatientRoundReview r) {
        return new PatientRoundReviewResponse(r.getId(), r.getRoundId(), r.getPatientId(),
                r.getReviewedById(), r.getReviewOrder(), r.getNewsScoreAtReview(),
                r.getClinicalStatus(), r.isWasExamined(), r.getManagementPlan(),
                r.getDischargeAssessment(), r.isNotifiedNextOfKin(), r.getReviewedAt(), r.getCreatedAt());
    }

    private void markPatientDischargeReady(Patient patient, Round round, String userId, String hospitalId) {
        patient.setDischargeReady(true);
        patient.setStatus(PatientStatus.DISCHARGE_READY);
        createDischargeTasks(patient, round, userId, hospitalId);

        outboxService.publish("careround.patient.discharge-ready",
                new PatientDischargeReadyEvent(hospitalId, patient.getId(), patient.getWardId(),
                        patient.getMedicalTeamId(), patient.getEstimatedDischargeDate(),
                        MDC.get("correlationId")),
                hospitalId);
    }

    private void createDischargeTasks(Patient patient, Round round, String userId, String hospitalId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime windowEnd = now.plusHours(24);

        careTaskRepository.save(dischargeTask(patient, round, userId, hospitalId,
                "DISCHARGE_SUMMARY", "Write discharge summary", AssignedToRole.JUNIOR_DOCTOR,
                now, windowEnd));
        careTaskRepository.save(dischargeTask(patient, round, userId, hospitalId,
                "DISCHARGE_MEDICATIONS", "Prepare discharge medications", AssignedToRole.NURSE,
                now, windowEnd));
    }

    private CareTask dischargeTask(Patient patient, Round round, String userId, String hospitalId,
                                   String taskType, String title, AssignedToRole assignedToRole,
                                   LocalDateTime windowStart, LocalDateTime windowEnd) {
        CareTask task = new CareTask();
        task.setHospitalId(hospitalId);
        task.setPatientId(patient.getId());
        task.setWardId(patient.getWardId());
        task.setRoundId(round.getId());
        task.setCreatedById(userId);
        task.setTaskType(taskType);
        task.setSource(TaskSource.POST_ROUND_JOB);
        task.setTitle(title);
        task.setPriority(TaskPriority.ROUTINE);
        task.setAssignedToRole(assignedToRole);
        task.setWindowStart(windowStart);
        task.setWindowEnd(windowEnd);
        return task;
    }
}
