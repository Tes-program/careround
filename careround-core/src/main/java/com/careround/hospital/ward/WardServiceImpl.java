package com.careround.hospital.ward;

import com.careround.hospital.entity.Handover;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.HandoverRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.UpdateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Escalation;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.Round;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.RoundRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WardServiceImpl implements WardService {

    private static final List<PatientStatus> ACTIVE_PATIENT_STATUSES =
            List.of(PatientStatus.ADMITTED, PatientStatus.STABLE, PatientStatus.DETERIORATING, PatientStatus.DISCHARGE_READY);
    private static final List<TaskStatus> OPEN_TASK_STATUSES =
            List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.OVERDUE);
    private static final List<EscalationStatus> OPEN_ESCALATION_STATUSES =
            List.of(EscalationStatus.OPEN, EscalationStatus.ACKNOWLEDGED);

    private final WardRepository wardRepository;
    private final PatientRepository patientRepository;
    private final CareTaskRepository careTaskRepository;
    private final EscalationRepository escalationRepository;
    private final ShiftRepository shiftRepository;
    private final RoundRepository roundRepository;
    private final HandoverRepository handoverRepository;

    @Override
    @Transactional
    public WardResponse create(String hospitalId, CreateWardRequest request) {
        Ward ward = new Ward();
        ward.setHospitalId(hospitalId);
        ward.setName(request.name());
        ward.setSpecialty(request.specialty());
        ward.setTotalBeds(request.totalBeds());
        ward.setSupervisorId(request.supervisorId());
        return toResponse(wardRepository.save(ward));
    }

    @Override
    @Transactional(readOnly = true)
    public WardResponse getById(String hospitalId, String wardId) {
        return wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(String hospitalId, String wardId) {
        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdAndStatusInOrderByNewsScoreDescAdmissionDateAsc(
                        hospitalId, wardId, ACTIVE_PATIENT_STATUSES);
        List<String> patientIds = patients.stream().map(Patient::getId).toList();
        List<CareTask> openTasks = careTaskRepository
                .findAllByHospitalIdAndWardIdAndStatusInOrderByWindowEndAsc(
                        hospitalId, wardId, OPEN_TASK_STATUSES);
        List<Escalation> openEscalations = patientIds.isEmpty()
                ? List.of()
                : escalationRepository.findAllByPatientIdInAndStatusInOrderBySeverityDescCreatedAtAsc(
                        patientIds, OPEN_ESCALATION_STATUSES);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Shift currentShift = shiftRepository
                .findFirstByWardIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeDesc(
                        wardId, ShiftStatus.ACTIVE, now, now)
                .orElse(null);
        List<Round> activeRounds = roundRepository
                .findAllByHospitalIdAndWardIdAndStatusOrderByStartedAtDesc(hospitalId, wardId, RoundStatus.IN_PROGRESS);
        List<Handover> recentHandovers = handoverRepository.findTop5ByWardIdOrderByCreatedAtDesc(wardId);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("ward", toResponse(ward));
        dashboard.put("activePatientCount", patients.size());
        dashboard.put("openTaskCount", openTasks.size());
        dashboard.put("overdueTaskCount", openTasks.stream().filter(this::isOverdue).count());
        dashboard.put("openEscalationCount", openEscalations.size());
        dashboard.put("bedOccupancy", Map.of(
                "occupied", patients.size(),
                "totalBeds", ward.getTotalBeds(),
                "available", Math.max(ward.getTotalBeds() - patients.size(), 0)));
        dashboard.put("currentShift", currentShift == null ? null : shiftMap(currentShift));
        dashboard.put("activePatients", patients.stream().map(this::patientMap).toList());
        dashboard.put("openTasks", openTasks.stream().map(this::taskMap).toList());
        dashboard.put("openEscalations", openEscalations.stream().map(this::escalationMap).toList());
        dashboard.put("activeRounds", activeRounds.stream().map(this::roundMap).toList());
        dashboard.put("handoverStatus", recentHandovers.isEmpty() ? null : handoverMap(recentHandovers.getFirst()));
        dashboard.put("recentHandovers", recentHandovers.stream().map(this::handoverMap).toList());
        return dashboard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardResponse> listByHospital(String hospitalId) {
        return wardRepository.findAllByHospitalId(hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public WardResponse update(String hospitalId, String wardId, UpdateWardRequest request) {
        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        if (request.name() != null) ward.setName(request.name());
        if (request.specialty() != null) ward.setSpecialty(request.specialty());
        if (request.totalBeds() != null) ward.setTotalBeds(request.totalBeds());
        if (request.supervisorId() != null) ward.setSupervisorId(request.supervisorId());
        return toResponse(ward);
    }

    @Override
    @Transactional
    public void delete(String hospitalId, String wardId) {
        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        wardRepository.delete(ward);
    }

    private WardResponse toResponse(Ward w) {
        return new WardResponse(w.getId(), w.getHospitalId(), w.getName(),
                w.getSpecialty(), w.getTotalBeds(), w.getSupervisorId(), w.getCreatedAt());
    }

    private boolean isOverdue(CareTask task) {
        return task.getStatus() == TaskStatus.OVERDUE
                || (task.getWindowEnd() != null
                && task.getWindowEnd().isBefore(LocalDateTime.now(ZoneOffset.UTC))
                && task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.CANCELLED);
    }

    private Map<String, Object> patientMap(Patient patient) {
        return mapOf(
                "id", patient.getId(),
                "wardId", patient.getWardId(),
                "medicalTeamId", patient.getMedicalTeamId(),
                "firstName", patient.getFirstName(),
                "lastName", patient.getLastName(),
                "hospitalNumber", patient.getHospitalNumber(),
                "bedNumber", patient.getBedNumber(),
                "primaryDiagnosis", patient.getPrimaryDiagnosis(),
                "acuityLevel", patient.getAcuityLevel(),
                "newsScore", patient.getNewsScore(),
                "status", patient.getStatus(),
                "isDischargeReady", patient.isDischargeReady(),
                "admissionDate", patient.getAdmissionDate());
    }

    private Map<String, Object> taskMap(CareTask task) {
        return mapOf(
                "id", task.getId(),
                "patientId", task.getPatientId(),
                "wardId", task.getWardId(),
                "roundId", task.getRoundId(),
                "title", task.getTitle(),
                "taskType", task.getTaskType(),
                "priority", task.getPriority(),
                "status", task.getStatus(),
                "assignedToId", task.getAssignedToId(),
                "assignedToRole", task.getAssignedToRole(),
                "windowStart", task.getWindowStart(),
                "windowEnd", task.getWindowEnd(),
                "workloadConflict", task.isWorkloadConflict(),
                "workloadConflictReason", task.getWorkloadConflictReason());
    }

    private Map<String, Object> escalationMap(Escalation escalation) {
        return mapOf(
                "id", escalation.getId(),
                "patientId", escalation.getPatientId(),
                "hospitalId", escalation.getHospitalId(),
                "triggeredById", escalation.getTriggeredById(),
                "assignedToId", escalation.getAssignedToId(),
                "triggerType", escalation.getTriggerType(),
                "severity", escalation.getSeverity(),
                "status", escalation.getStatus(),
                "notes", escalation.getNotes(),
                "createdAt", escalation.getCreatedAt(),
                "resolvedAt", escalation.getResolvedAt());
    }

    private Map<String, Object> shiftMap(Shift shift) {
        return mapOf(
                "id", shift.getId(),
                "wardId", shift.getWardId(),
                "shiftScheduleId", shift.getShiftScheduleId(),
                "type", shift.getType(),
                "label", shift.getType() + " shift",
                "windowLabel", "%s-%s".formatted(shift.getStartTime().toLocalTime(), shift.getEndTime().toLocalTime()),
                "startTime", shift.getStartTime(),
                "endTime", shift.getEndTime(),
                "leadDoctorId", shift.getLeadDoctorId(),
                "nurseInChargeId", shift.getNurseInChargeId(),
                "status", shift.getStatus(),
                "assignedAt", shift.getAssignedAt());
    }

    private Map<String, Object> roundMap(Round round) {
        return mapOf(
                "id", round.getId(),
                "hospitalId", round.getHospitalId(),
                "wardId", round.getWardId(),
                "medicalTeamId", round.getMedicalTeamId(),
                "shiftId", round.getShiftId(),
                "roundType", round.getRoundType(),
                "leadDoctorId", round.getLeadDoctorId(),
                "status", round.getStatus(),
                "scheduledTime", round.getScheduledTime(),
                "startedAt", round.getStartedAt(),
                "completedAt", round.getCompletedAt(),
                "teamMembers", round.getTeamMembers());
    }

    private Map<String, Object> handoverMap(Handover handover) {
        return mapOf(
                "id", handover.getId(),
                "wardId", handover.getWardId(),
                "outgoingShiftId", handover.getOutgoingShiftId(),
                "incomingShiftId", handover.getIncomingShiftId(),
                "conductedById", handover.getConductedById(),
                "status", handover.getStatus(),
                "generalNotes", handover.getGeneralNotes(),
                "completedAt", handover.getCompletedAt(),
                "createdAt", handover.getCreatedAt());
    }

    private Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((String) entries[i], entries[i + 1]);
        }
        return map;
    }
}
