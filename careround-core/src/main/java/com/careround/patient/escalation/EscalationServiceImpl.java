package com.careround.patient.escalation;

import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.patient.entity.Escalation;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.EscalationTrigger;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.escalation.dto.AcknowledgeEscalationRequest;
import com.careround.patient.escalation.dto.CreateEscalationRequest;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.patient.escalation.dto.ResolveEscalationRequest;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.PatientDeteriorationEvent;
import com.careround.shared.exception.AccessDeniedException;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscalationServiceImpl implements EscalationService {

    private final EscalationRepository escalationRepository;
    private final PatientRepository patientRepository;
    private final MedicalTeamRepository medicalTeamRepository;
    private final OnCallRotationRepository onCallRotationRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public EscalationResponse triggerSystemEscalation(String patientId, EscalationSeverity severity, String notes) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Optional<Escalation> existing = escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                patientId, EscalationStatus.OPEN, sameOrHigherSeverities(severity));
        if (existing.isPresent()) {
            log.info("action=escalationSkippedDuplicate patientId={} severity={}", patientId, severity);
            return toResponse(existing.get());
        }

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Escalation escalation = new Escalation();
        escalation.setHospitalId(hospitalId);
        escalation.setPatientId(patientId);
        escalation.setSeverity(severity);
        escalation.setTriggerType(EscalationTrigger.HIGH_NEWS_SCORE);
        escalation.setStatus(EscalationStatus.OPEN);
        escalation.setNotes(notes);
        escalation.setAssignedToId(resolveOnCallAssignee(hospitalId, patient, severity));

        Escalation saved = escalationRepository.save(escalation);

        PatientDeteriorationEvent event = new PatientDeteriorationEvent(
                hospitalId, patientId, patient.getWardId(),
                patient.getNewsScore(), severity, saved.getId(),
                MDC.get("correlationId"));
        outboxService.publish("careround.patient.deterioration", event, hospitalId);

        log.info("action=systemEscalationCreated patientId={} hospitalId={} severity={} escalationId={}",
                patientId, hospitalId, severity, saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EscalationResponse createManualEscalation(CreateEscalationRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String triggeredById = HospitalContextHolder.getUserId();
        String patientId = request.patientId();

        Optional<Escalation> existing = escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                patientId, EscalationStatus.OPEN, sameOrHigherSeverities(request.severity()));
        if (existing.isPresent()) {
            log.info("action=manualEscalationSkippedDuplicate patientId={} severity={}", patientId, request.severity());
            return toResponse(existing.get());
        }

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Escalation escalation = new Escalation();
        escalation.setHospitalId(hospitalId);
        escalation.setPatientId(patientId);
        escalation.setTriggeredById(triggeredById);
        escalation.setSeverity(request.severity());
        escalation.setTriggerType(request.triggerType());
        escalation.setStatus(EscalationStatus.OPEN);
        escalation.setNotes(request.notes());
        escalation.setAssignedToId(resolveOnCallAssignee(hospitalId, patient, request.severity()));

        Escalation saved = escalationRepository.save(escalation);

        PatientDeteriorationEvent event = new PatientDeteriorationEvent(
                hospitalId, patientId, patient.getWardId(),
                patient.getNewsScore(), request.severity(), saved.getId(),
                MDC.get("correlationId"));
        outboxService.publish("careround.patient.deterioration", event, hospitalId);

        log.info("action=manualEscalationCreated patientId={} hospitalId={} severity={} escalationId={}",
                patientId, hospitalId, request.severity(), saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EscalationResponse acknowledgeEscalation(String escalationId, AcknowledgeEscalationRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Escalation escalation = escalationRepository.findByIdAndHospitalId(escalationId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation not found"));

        escalation.setStatus(EscalationStatus.ACKNOWLEDGED);
        if (request.notes() != null) escalation.setNotes(request.notes());

        log.info("action=escalationAcknowledged escalationId={} hospitalId={}", escalationId, hospitalId);
        return toResponse(escalation);
    }

    @Override
    @Transactional
    public EscalationResponse resolveEscalation(String escalationId, ResolveEscalationRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Escalation escalation = escalationRepository.findByIdAndHospitalId(escalationId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation not found"));

        escalation.setStatus(EscalationStatus.RESOLVED);
        escalation.setResolvedAt(LocalDateTime.now(ZoneOffset.UTC));
        escalation.setNotes(request.notes());

        log.info("action=escalationResolved escalationId={} hospitalId={}", escalationId, hospitalId);
        return toResponse(escalation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscalationResponse> getOpenEscalations(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdAndStatusOrderByNewsScoreDescAdmissionDateAsc(
                        hospitalId, wardId, PatientStatus.ADMITTED);
        if (patients.isEmpty()) return List.of();

        List<String> patientIds = patients.stream().map(Patient::getId).toList();
        return escalationRepository.findAllByPatientIdInAndStatusInOrderBySeverityDescCreatedAtAsc(
                        patientIds, List.of(EscalationStatus.OPEN, EscalationStatus.ACKNOWLEDGED))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscalationResponse> getEscalationsByPatient(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return escalationRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    private String resolveOnCallAssignee(String hospitalId, Patient patient, EscalationSeverity severity) {
        OnCallRole onCallRole = severity == EscalationSeverity.RED
                ? OnCallRole.CONSULTANT_ON_CALL : OnCallRole.REGISTRAR_ON_CALL;

        MedicalTeam team = medicalTeamRepository.findByIdAndHospitalId(patient.getMedicalTeamId(), hospitalId)
                .orElse(null);
        if (team == null) return null;

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return onCallRotationRepository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                        hospitalId, team.getDepartmentId(), onCallRole, now, now)
                .map(OnCallRotation::getDoctorId)
                .orElse(null);
    }

    private List<EscalationSeverity> sameOrHigherSeverities(EscalationSeverity severity) {
        if (severity == EscalationSeverity.RED) {
            return List.of(EscalationSeverity.RED);
        }
        return List.of(EscalationSeverity.AMBER, EscalationSeverity.RED);
    }

    private EscalationResponse toResponse(Escalation e) {
        return new EscalationResponse(
                e.getId(), e.getPatientId(), e.getHospitalId(),
                e.getTriggeredById(), e.getAssignedToId(),
                e.getTriggerType(), e.getSeverity(), e.getStatus(),
                e.getNotes(), e.getResolvedAt(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
