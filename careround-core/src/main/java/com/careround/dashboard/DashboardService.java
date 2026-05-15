package com.careround.dashboard;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.DepartmentRepository;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.ShiftScheduleRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Escalation;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.RoundRepository;
import com.careround.notification.service.NotificationService;
import com.careround.shared.security.HospitalContextHolder;
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
public class DashboardService {

    private static final List<TaskStatus> OPEN_TASK_STATUSES = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
    private static final List<TaskStatus> SUPERVISOR_OPEN_TASK_STATUSES =
            List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.OVERDUE);
    private static final List<EscalationStatus> OPEN_ESCALATION_STATUSES =
            List.of(EscalationStatus.OPEN, EscalationStatus.ACKNOWLEDGED);
    private static final List<PatientStatus> ACTIVE_PATIENT_STATUSES =
            List.of(PatientStatus.ADMITTED, PatientStatus.STABLE, PatientStatus.DETERIORATING, PatientStatus.DISCHARGE_READY);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final WardRepository wardRepository;
    private final ShiftScheduleRepository shiftScheduleRepository;
    private final ShiftRepository shiftRepository;
    private final MedicalTeamRepository medicalTeamRepository;
    private final PatientRepository patientRepository;
    private final CareTaskRepository careTaskRepository;
    private final EscalationRepository escalationRepository;
    private final RoundRepository roundRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Map<String, Object> currentUserDashboard() {
        return dashboardForRole(HospitalContextHolder.getRole());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboardForRole(UserRole role) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();
        List<Ward> wards = wardRepository.findAllByHospitalId(hospitalId);
        List<String> wardIds = wards.stream().map(Ward::getId).toList();

        Map<String, Object> dashboard = baseDashboard(hospitalId, role, wardIds);
        switch (role) {
            case ADMIN -> addAdminDashboard(dashboard, hospitalId);
            case CONSULTANT -> addConsultantDashboard(dashboard, hospitalId, userId);
            case REGISTRAR, JUNIOR_DOCTOR -> addDoctorDashboard(dashboard, hospitalId, userId);
            case NURSE -> addNurseDashboard(dashboard, hospitalId, userId);
            case WARD_SUPERVISOR -> addWardSupervisorDashboard(dashboard, hospitalId, userId, wards);
            default -> {
            }
        }
        return dashboard;
    }

    private Map<String, Object> baseDashboard(String hospitalId, UserRole role, List<String> wardIds) {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("hospitalId", hospitalId);
        dashboard.put("role", role.name());
        dashboard.put("activePatients", patientRepository.countByHospitalIdAndStatus(hospitalId, PatientStatus.ADMITTED));
        dashboard.put("openEscalations", escalationRepository.countByHospitalIdAndStatusIn(hospitalId, OPEN_ESCALATION_STATUSES));
        dashboard.put("openTasks", careTaskRepository.countByHospitalIdAndStatusIn(hospitalId, OPEN_TASK_STATUSES));
        dashboard.put("overdueTasks", careTaskRepository.countByHospitalIdAndStatusInAndWindowEndBefore(
                hospitalId, OPEN_TASK_STATUSES, LocalDateTime.now(ZoneOffset.UTC)));
        dashboard.put("activeShifts", wardIds.isEmpty() ? 0 : shiftRepository.countByWardIdInAndStatus(
                wardIds, com.careround.hospital.enums.ShiftStatus.ACTIVE));
        dashboard.put("roundsInProgress", roundRepository.countByHospitalIdAndStatus(hospitalId, RoundStatus.IN_PROGRESS));
        var notifications = notificationService.listNotifications();
        dashboard.put("unreadNotifications", notifications.stream().filter(notification -> !notification.read()).count());
        dashboard.put("recentNotifications", notifications.stream().limit(5).toList());
        return dashboard;
    }

    private void addAdminDashboard(Map<String, Object> dashboard, String hospitalId) {
        dashboard.put("activeUsers", userRepository.countByHospitalIdAndIsActiveTrue(hospitalId));
        dashboard.put("departments", departmentRepository.countByHospitalId(hospitalId));
        dashboard.put("wards", wardRepository.countByHospitalId(hospitalId));
        dashboard.put("medicalTeams", medicalTeamRepository.countByHospitalId(hospitalId));
        dashboard.put("activeShiftSchedules", shiftScheduleRepository.countByHospitalIdAndIsActiveTrue(hospitalId));
    }

    private void addConsultantDashboard(Map<String, Object> dashboard, String hospitalId, String userId) {
        List<String> teamIds = medicalTeamRepository.findAllByConsultantIdAndHospitalId(userId, hospitalId)
                .stream()
                .map(team -> team.getId())
                .toList();
        dashboard.put("teamsLed", teamIds.size());
        dashboard.put("activeTeamPatients", teamIds.isEmpty() ? 0 :
                patientRepository.countByHospitalIdAndMedicalTeamIdInAndStatus(hospitalId, teamIds, PatientStatus.ADMITTED));
        dashboard.put("teamPatients", teamIds.isEmpty() ? List.of() :
                patientRepository.findAllByHospitalIdAndMedicalTeamIdInAndStatusInOrderByMedicalTeamIdAscNewsScoreDescAdmissionDateAsc(
                                hospitalId, teamIds, ACTIVE_PATIENT_STATUSES)
                        .stream()
                        .map(this::patientMap)
                        .toList());
        addDoctorDashboard(dashboard, hospitalId, userId);
    }

    private void addDoctorDashboard(Map<String, Object> dashboard, String hospitalId, String userId) {
        dashboard.put("assignedOpenTasks",
                careTaskRepository.countByHospitalIdAndAssignedToIdAndStatusIn(hospitalId, userId, OPEN_TASK_STATUSES));
        dashboard.put("assignedOpenEscalations",
                escalationRepository.countByHospitalIdAndAssignedToIdAndStatusIn(hospitalId, userId, OPEN_ESCALATION_STATUSES));
    }

    private void addNurseDashboard(Map<String, Object> dashboard, String hospitalId, String userId) {
        dashboard.put("assignedOpenTasks",
                careTaskRepository.countByHospitalIdAndAssignedToIdAndStatusIn(hospitalId, userId, OPEN_TASK_STATUSES));
    }

    private void addWardSupervisorDashboard(Map<String, Object> dashboard, String hospitalId, String userId, List<Ward> wards) {
        List<String> supervisedWardIds = wards.stream()
                .filter(ward -> userId.equals(ward.getSupervisorId()))
                .map(Ward::getId)
                .toList();

        dashboard.put("supervisedWards", supervisedWardIds.size());
        dashboard.put("activeWardPatients", supervisedWardIds.isEmpty() ? 0 :
                patientRepository.countByHospitalIdAndWardIdInAndStatus(hospitalId, supervisedWardIds, PatientStatus.ADMITTED));
        dashboard.put("openWardTasks", supervisedWardIds.isEmpty() ? 0 :
                careTaskRepository.countByHospitalIdAndWardIdInAndStatusIn(hospitalId, supervisedWardIds, OPEN_TASK_STATUSES));
        dashboard.put("overdueWardTasks", supervisedWardIds.isEmpty() ? 0 :
                careTaskRepository.countByHospitalIdAndWardIdInAndStatusInAndWindowEndBefore(
                        hospitalId, supervisedWardIds, OPEN_TASK_STATUSES, LocalDateTime.now(ZoneOffset.UTC)));
        if (!supervisedWardIds.isEmpty()) {
            addWardSupervisorCollections(dashboard, hospitalId, supervisedWardIds);
        } else {
            dashboard.put("wardPatients", List.of());
            dashboard.put("wardTasks", List.of());
            dashboard.put("wardEscalations", List.of());
            dashboard.put("currentShifts", List.of());
        }
    }

    private void addWardSupervisorCollections(Map<String, Object> dashboard, String hospitalId, List<String> wardIds) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdInAndStatusInOrderByWardIdAscNewsScoreDescAdmissionDateAsc(
                        hospitalId, wardIds, ACTIVE_PATIENT_STATUSES);
        List<String> patientIds = patients.stream().map(Patient::getId).toList();
        List<CareTask> tasks = careTaskRepository
                .findAllByHospitalIdAndWardIdInAndStatusInOrderByWindowEndAsc(
                        hospitalId, wardIds, SUPERVISOR_OPEN_TASK_STATUSES);
        List<Escalation> escalations = patientIds.isEmpty()
                ? List.of()
                : escalationRepository.findAllByPatientIdInAndStatusInOrderBySeverityDescCreatedAtAsc(
                        patientIds, OPEN_ESCALATION_STATUSES);
        List<Shift> shifts = shiftRepository
                .findAllByWardIdInAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByWardIdAscStartTimeDesc(
                        wardIds, ShiftStatus.ACTIVE, now, now);

        dashboard.put("wardPatients", patients.stream().map(this::patientMap).toList());
        dashboard.put("wardTasks", tasks.stream().map(this::taskMap).toList());
        dashboard.put("wardEscalations", escalations.stream().map(this::escalationMap).toList());
        dashboard.put("currentShifts", shifts.stream().map(this::shiftMap).toList());
    }

    private Map<String, Object> patientMap(Patient patient) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", patient.getId());
        map.put("wardId", patient.getWardId());
        map.put("medicalTeamId", patient.getMedicalTeamId());
        map.put("firstName", patient.getFirstName());
        map.put("lastName", patient.getLastName());
        map.put("hospitalNumber", patient.getHospitalNumber());
        map.put("bedNumber", patient.getBedNumber());
        map.put("primaryDiagnosis", patient.getPrimaryDiagnosis());
        map.put("acuityLevel", patient.getAcuityLevel());
        map.put("newsScore", patient.getNewsScore());
        map.put("status", patient.getStatus());
        map.put("isDischargeReady", patient.isDischargeReady());
        map.put("admissionDate", patient.getAdmissionDate());
        return map;
    }

    private Map<String, Object> taskMap(CareTask task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", task.getId());
        map.put("patientId", task.getPatientId());
        map.put("wardId", task.getWardId());
        map.put("roundId", task.getRoundId());
        map.put("title", task.getTitle());
        map.put("taskType", task.getTaskType());
        map.put("priority", task.getPriority());
        map.put("status", task.getStatus());
        map.put("assignedToId", task.getAssignedToId());
        map.put("assignedToRole", task.getAssignedToRole());
        map.put("windowStart", task.getWindowStart());
        map.put("windowEnd", task.getWindowEnd());
        map.put("workloadConflict", task.isWorkloadConflict());
        map.put("workloadConflictReason", task.getWorkloadConflictReason());
        return map;
    }

    private Map<String, Object> escalationMap(Escalation escalation) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", escalation.getId());
        map.put("patientId", escalation.getPatientId());
        map.put("hospitalId", escalation.getHospitalId());
        map.put("triggeredById", escalation.getTriggeredById());
        map.put("assignedToId", escalation.getAssignedToId());
        map.put("triggerType", escalation.getTriggerType());
        map.put("severity", escalation.getSeverity());
        map.put("status", escalation.getStatus());
        map.put("notes", escalation.getNotes());
        map.put("createdAt", escalation.getCreatedAt());
        map.put("resolvedAt", escalation.getResolvedAt());
        return map;
    }

    private Map<String, Object> shiftMap(Shift shift) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", shift.getId());
        map.put("wardId", shift.getWardId());
        map.put("shiftScheduleId", shift.getShiftScheduleId());
        map.put("type", shift.getType());
        map.put("label", shift.getType() + " shift");
        map.put("windowLabel", "%s-%s".formatted(shift.getStartTime().toLocalTime(), shift.getEndTime().toLocalTime()));
        map.put("startTime", shift.getStartTime());
        map.put("endTime", shift.getEndTime());
        map.put("leadDoctorId", shift.getLeadDoctorId());
        map.put("nurseInChargeId", shift.getNurseInChargeId());
        map.put("status", shift.getStatus());
        map.put("assignedAt", shift.getAssignedAt());
        return map;
    }
}
