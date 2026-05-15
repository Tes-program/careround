package com.careround.patient.patient;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.Department;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.repository.DepartmentRepository;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.entity.Escalation;
import com.careround.patient.entity.PatientRoundReview;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.MarkDischargeReadyRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.PatientTimelineItemResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientRoundReviewRepository;
import com.careround.patient.repository.PatientVitalsRepository;
import com.careround.shared.event.PatientAdmittedEvent;
import com.careround.shared.event.PatientDischargedEvent;
import com.careround.shared.event.PatientDischargeReadyEvent;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private static final List<PatientStatus> ACTIVE_PATIENT_STATUSES =
            List.of(PatientStatus.ADMITTED, PatientStatus.STABLE, PatientStatus.DETERIORATING, PatientStatus.DISCHARGE_READY);

    private final PatientRepository patientRepository;
    private final WardRepository wardRepository;
    private final MedicalTeamRepository medicalTeamRepository;
    private final DepartmentRepository departmentRepository;
    private final OnCallRotationRepository onCallRotationRepository;
    private final CareTaskRepository careTaskRepository;
    private final PatientVitalsRepository patientVitalsRepository;
    private final EscalationRepository escalationRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final PatientRoundReviewRepository patientRoundReviewRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public PatientResponse admitPatient(AdmitPatientRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Ward ward = wardRepository.findById(request.wardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        if (!ward.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Ward does not belong to this hospital");
        }

        MedicalTeam team = medicalTeamRepository.findById(request.medicalTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
        if (!team.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Medical team does not belong to this hospital");
        }

        String consultantId = resolveConsultant(hospitalId, request);

        Patient patient = new Patient();
        patient.setHospitalId(hospitalId);
        patient.setWardId(request.wardId());
        patient.setMedicalTeamId(request.medicalTeamId());
        patient.setAdmittingConsultantId(consultantId);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setHospitalNumber(request.hospitalNumber());
        patient.setAdmissionType(request.admissionType());
        patient.setPrimaryDiagnosis(request.primaryDiagnosis());
        patient.setSpecialtyRequired(request.specialtyRequired());
        patient.setEstimatedDischargeDate(request.estimatedDischargeDate());
        patient.setStatus(PatientStatus.ADMITTED);
        patient.setAdmissionDate(LocalDateTime.now(ZoneOffset.UTC));

        Patient saved = patientRepository.save(patient);

        PatientAdmittedEvent event = new PatientAdmittedEvent(
                hospitalId, saved.getId(), saved.getWardId(),
                saved.getMedicalTeamId(), saved.getAdmittingConsultantId(),
                MDC.get("correlationId"));
        outboxService.publish("careround.patient.admitted", event, hospitalId);

        log.info("action=admitPatient patientId={} hospitalId={} wardId={}", saved.getId(), hospitalId, saved.getWardId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatient(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Access denied: patient belongs to another hospital");
        }
        return toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientTimelineItemResponse> getPatientTimeline(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        List<PatientTimelineItemResponse> timeline = new ArrayList<>();
        timeline.add(new PatientTimelineItemResponse(
                patient.getId(),
                "ADMISSION",
                "Patient admitted",
                patient.getPrimaryDiagnosis(),
                patient.getAdmissionDate(),
                patient.getAdmittingConsultantId(),
                metadata(
                        "wardId", patient.getWardId(),
                        "medicalTeamId", patient.getMedicalTeamId(),
                        "admissionType", patient.getAdmissionType(),
                        "status", patient.getStatus())));

        for (PatientVitals vitals : patientVitalsRepository.findAllByPatientIdOrderByRecordedAtDesc(patientId)) {
            timeline.add(new PatientTimelineItemResponse(
                    vitals.getId(),
                    "VITALS",
                    "Vitals recorded",
                    "NEWS score " + vitals.getNewsScore(),
                    vitals.getRecordedAt(),
                    vitals.getRecordedById(),
                    metadata(
                            "newsScore", vitals.getNewsScore(),
                            "heartRate", vitals.getHeartRate(),
                            "respiratoryRate", vitals.getRespiratoryRate(),
                            "oxygenSaturation", vitals.getOxygenSaturation(),
                            "systolicBP", vitals.getSystolicBP(),
                            "temperature", vitals.getTemperature(),
                            "consciousnessLevel", vitals.getConsciousnessLevel())));
        }

        for (Escalation escalation : escalationRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)) {
            timeline.add(new PatientTimelineItemResponse(
                    escalation.getId(),
                    "ESCALATION",
                    escalation.getSeverity() + " escalation " + escalation.getStatus(),
                    escalation.getNotes(),
                    escalation.getCreatedAt(),
                    escalation.getTriggeredById(),
                    metadata(
                            "severity", escalation.getSeverity(),
                            "status", escalation.getStatus(),
                            "triggerType", escalation.getTriggerType(),
                            "assignedToId", escalation.getAssignedToId(),
                            "resolvedAt", escalation.getResolvedAt())));
        }

        for (PatientRoundReview review : patientRoundReviewRepository.findAllByPatientIdOrderByReviewedAtDesc(patientId)) {
            timeline.add(new PatientTimelineItemResponse(
                    review.getId(),
                    "ROUND_REVIEW",
                    "Round review",
                    review.getManagementPlan(),
                    review.getReviewedAt(),
                    review.getReviewedById(),
                    metadata(
                            "roundId", review.getRoundId(),
                            "clinicalStatus", review.getClinicalStatus(),
                            "newsScoreAtReview", review.getNewsScoreAtReview(),
                            "dischargeAssessment", review.getDischargeAssessment(),
                            "wasExamined", review.isWasExamined(),
                            "notifiedNextOfKin", review.isNotifiedNextOfKin())));
        }

        for (ClinicalNote note : clinicalNoteRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)) {
            timeline.add(new PatientTimelineItemResponse(
                    note.getId(),
                    "CLINICAL_NOTE",
                    note.getNoteType() + " note",
                    note.getContent(),
                    note.getCreatedAt(),
                    note.getAuthorId(),
                    metadata(
                            "patientRoundReviewId", note.getPatientRoundReviewId(),
                            "vitalsId", note.getVitalsId(),
                            "isAmended", note.isAmended(),
                            "amendedById", note.getAmendedById(),
                            "amendedAt", note.getAmendedAt())));
        }

        for (CareTask task : careTaskRepository.findAllByPatientId(patientId)) {
            LocalDateTime occurredAt = task.getCompletedAt() != null ? task.getCompletedAt() : task.getCreatedAt();
            timeline.add(new PatientTimelineItemResponse(
                    task.getId(),
                    "CARE_TASK",
                    task.getTitle(),
                    task.getDescription(),
                    occurredAt,
                    task.getCompletedById() != null ? task.getCompletedById() : task.getCreatedById(),
                    metadata(
                            "wardId", task.getWardId(),
                            "roundId", task.getRoundId(),
                            "assignedToId", task.getAssignedToId(),
                            "assignedToRole", task.getAssignedToRole(),
                            "priority", task.getPriority(),
                            "status", task.getStatus(),
                            "windowStart", task.getWindowStart(),
                            "windowEnd", task.getWindowEnd(),
                            "workloadConflict", task.isWorkloadConflict())));
        }

        return timeline.stream()
                .sorted(Comparator.comparing(PatientTimelineItemResponse::occurredAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByWard(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        return patientRepository.findAllByHospitalIdAndWardIdAndStatusOrderByNewsScoreDescAdmissionDateAsc(
                        hospitalId, wardId, PatientStatus.ADMITTED)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByTeam(String teamId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        medicalTeamRepository.findByIdAndHospitalId(teamId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
        return patientRepository
                .findAllByHospitalIdAndMedicalTeamIdAndStatusInOrderByNewsScoreDescAdmissionDateAsc(
                        hospitalId, teamId, ACTIVE_PATIENT_STATUSES)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String query) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        return patientRepository.searchByHospitalId(hospitalId, query)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PatientResponse markDischargeReady(String patientId, MarkDischargeReadyRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        if (!HospitalContextHolder.hasRole(UserRole.CONSULTANT)) {
            throw new AccessDeniedException("Only consultants can mark patients as discharge ready");
        }

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patient.setDischargeReady(true);
        patient.setStatus(PatientStatus.DISCHARGE_READY);
        if (request.estimatedDischargeDate() != null) {
            patient.setEstimatedDischargeDate(request.estimatedDischargeDate());
        }

        PatientDischargeReadyEvent event = new PatientDischargeReadyEvent(
                hospitalId, patientId, patient.getWardId(),
                patient.getMedicalTeamId(), patient.getEstimatedDischargeDate(),
                MDC.get("correlationId"));
        outboxService.publish("careround.patient.discharge-ready", event, hospitalId);

        log.info("action=markDischargeReady patientId={} hospitalId={}", patientId, hospitalId);
        return toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse updatePatientStatus(String patientId, UpdatePatientStatusRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        PatientStatus current = patient.getStatus();
        PatientStatus target = request.status();

        if (target == PatientStatus.ADMITTED) {
            throw new BusinessRuleException("Cannot transition to ADMITTED");
        }
        if (current == PatientStatus.DISCHARGED) {
            throw new BusinessRuleException("Patient is already discharged");
        }
        if (current == PatientStatus.DISCHARGE_READY && target != PatientStatus.DISCHARGED) {
            throw new BusinessRuleException("Discharge-ready patients can only transition to DISCHARGED");
        }
        if (current != PatientStatus.ADMITTED && current != PatientStatus.DISCHARGE_READY) {
            throw new BusinessRuleException("Unsupported patient status transition");
        }

        if (target == PatientStatus.DISCHARGED) {
            if (current != PatientStatus.DISCHARGE_READY || !patient.isDischargeReady()) {
                throw new BusinessRuleException("Patient must be marked discharge ready before discharge");
            }
            long incomplete = careTaskRepository.findAllByPatientId(patientId).stream()
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                    .count();
            if (incomplete > 0) {
                throw new BusinessRuleException("Cannot discharge patient: " + incomplete + " incomplete care task(s) remain");
            }

            LocalDateTime dischargedAt = LocalDateTime.now(ZoneOffset.UTC);
            String dischargedFromWardId = patient.getWardId();
            outboxService.publish("careround.patient.discharged",
                    new PatientDischargedEvent(hospitalId, patientId, dischargedFromWardId,
                            dischargedAt, MDC.get("correlationId")),
                    hospitalId);
            patient.setWardId(null);
            patient.setBedNumber(null);
        }

        patient.setStatus(target);
        log.info("action=updatePatientStatus patientId={} hospitalId={} status={}", patientId, hospitalId, target);
        return toResponse(patient);
    }

    private String resolveConsultant(String hospitalId, AdmitPatientRequest request) {
        if (request.admittingConsultantId() != null) {
            return request.admittingConsultantId();
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Department> departments = departmentRepository.findAllByHospitalId(hospitalId);

        String consultantId = findOnCallConsultant(departments, request.specialtyRequired(), hospitalId, now);
        if (consultantId != null) return consultantId;

        consultantId = findOnCallConsultant(departments, "General Medicine", hospitalId, now);
        if (consultantId != null) return consultantId;

        throw new BusinessRuleException("No on-call consultant found for specialty: " + request.specialtyRequired());
    }

    private String findOnCallConsultant(List<Department> departments, String specialty, String hospitalId, LocalDateTime now) {
        return departments.stream()
                .filter(d -> d.getName().equalsIgnoreCase(specialty))
                .findFirst()
                .flatMap(dept -> onCallRotationRepository
                        .findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                                hospitalId, dept.getId(), OnCallRole.CONSULTANT_ON_CALL, now, now))
                .map(OnCallRotation::getDoctorId)
                .orElse(null);
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(), p.getWardId(), p.getMedicalTeamId(), p.getAdmittingConsultantId(),
                p.getFirstName(), p.getLastName(), p.getHospitalNumber(), p.getDateOfBirth(),
                p.getGender(), p.getBedNumber(), p.getAdmissionType(), p.getPrimaryDiagnosis(),
                p.getSpecialtyRequired(), p.getAcuityLevel(), p.getNewsScore(),
                p.isDischargeReady(), p.getEstimatedDischargeDate(), p.getStatus(),
                p.getAdmissionDate(), p.getCreatedAt(), p.getUpdatedAt());
    }

    private Map<String, Object> metadata(Object... entries) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            metadata.put((String) entries[i], entries[i + 1]);
        }
        return metadata;
    }
}
