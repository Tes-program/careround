package com.careround.dashboard;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.DepartmentRepository;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.ShiftScheduleRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.RoundRepository;
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
    private static final List<EscalationStatus> OPEN_ESCALATION_STATUSES =
            List.of(EscalationStatus.OPEN, EscalationStatus.ACKNOWLEDGED);

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
    }
}
