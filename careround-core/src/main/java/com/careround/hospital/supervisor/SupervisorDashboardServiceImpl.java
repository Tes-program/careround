package com.careround.hospital.supervisor;

import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.supervisor.dto.HourlyTaskCount;
import com.careround.hospital.supervisor.dto.OverdueAlert;
import com.careround.hospital.supervisor.dto.PatientSummary;
import com.careround.hospital.supervisor.dto.SupervisorDashboardResponse;
import com.careround.hospital.supervisor.dto.TaskStats;
import com.careround.patient.entity.Patient;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupervisorDashboardServiceImpl implements SupervisorDashboardService {

    private static final int HOURLY_CHART_HOURS = 24;

    private final WardRepository wardRepository;
    private final PatientRepository patientRepository;
    private final MedicationTaskRepository medicationTaskRepository;

    @Override
    @Transactional(readOnly = true)
    public SupervisorDashboardResponse getDashboard(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(hospitalId, wardId);

        Map<String, Patient> patientById = patients.stream()
                .collect(Collectors.toMap(Patient::getId, p -> p));

        List<PatientSummary> summaries = patients.stream()
                .map(p -> new PatientSummary(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAcuityColor() != null ? p.getAcuityColor().name() : null,
                        p.getAdmissionDate() != null
                                ? p.getAdmissionDate().atOffset(ZoneOffset.UTC).toString() : null,
                        p.getWardId()))
                .toList();

        TaskStats taskStats = buildTaskStats(wardId, hospitalId);
        List<OverdueAlert> overdueAlerts = buildOverdueAlerts(wardId, hospitalId, patientById);
        List<HourlyTaskCount> hourlyChart = buildHourlyChart(wardId, hospitalId);

        return new SupervisorDashboardResponse(
                ward.getId(), ward.getName(), ward.getSpecialty(),
                ward.getTotalBeds(), summaries.size(),
                summaries, taskStats, overdueAlerts, hourlyChart);
    }

    private TaskStats buildTaskStats(String wardId, String hospitalId) {
        long pending = medicationTaskRepository.countByWardIdAndHospitalIdAndStatus(
                wardId, hospitalId, MedicationTaskStatus.PENDING);
        long overdue = medicationTaskRepository.countByWardIdAndHospitalIdAndStatus(
                wardId, hospitalId, MedicationTaskStatus.OVERDUE);

        LocalDateTime startOfDay = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long completedToday = medicationTaskRepository.countByWardIdAndHospitalIdAndStatusAndCompletedAtBetween(
                wardId, hospitalId, MedicationTaskStatus.COMPLETED, startOfDay, endOfDay);

        return new TaskStats((int) pending, (int) overdue, (int) completedToday);
    }

    private List<OverdueAlert> buildOverdueAlerts(String wardId, String hospitalId,
                                                   Map<String, Patient> patientById) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<MedicationTask> overdueTasks = medicationTaskRepository
                .findAllByWardIdAndHospitalIdAndStatusIn(
                        wardId, hospitalId, List.of(MedicationTaskStatus.OVERDUE));

        return overdueTasks.stream()
                .map(t -> {
                    Patient p = patientById.get(t.getPatientId());
                    String patientName = p != null
                            ? p.getFirstName() + " " + p.getLastName() : "Unknown";
                    long minutesOverdueLong = ChronoUnit.MINUTES.between(t.getScheduledTime(), now);
                    int minutesOverdue = minutesOverdueLong > Integer.MAX_VALUE
                            ? Integer.MAX_VALUE : (int) minutesOverdueLong;
                    String scheduledTimeStr = t.getScheduledTime() != null
                            ? t.getScheduledTime().atOffset(ZoneOffset.UTC).toString() : null;
                    return new OverdueAlert(t.getId(), t.getPatientId(), patientName,
                            t.getAssignedNurseId(), scheduledTimeStr, minutesOverdue);
                })
                .sorted(java.util.Comparator.comparingInt(OverdueAlert::minutesOverdue).reversed())
                .toList();
    }

    private List<HourlyTaskCount> buildHourlyChart(String wardId, String hospitalId) {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<MedicationTask> completedToday = medicationTaskRepository
                .findAllByWardIdAndHospitalIdAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                        wardId, hospitalId, MedicationTaskStatus.COMPLETED, startOfDay, endOfDay);
        if (completedToday == null) completedToday = List.of();

        Map<LocalDateTime, Long> countsByHour = completedToday.stream()
                .filter(t -> t.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCompletedAt().truncatedTo(ChronoUnit.HOURS),
                        Collectors.counting()));

        List<HourlyTaskCount> chart = new ArrayList<>();
        for (int i = 0; i < HOURLY_CHART_HOURS; i++) {
            LocalDateTime hour = startOfDay.plusHours(i);
            String hourStr = hour.atOffset(ZoneOffset.UTC).toString();
            chart.add(new HourlyTaskCount(hourStr, countsByHour.getOrDefault(hour, 0L).intValue()));
        }
        return chart;
    }
}
