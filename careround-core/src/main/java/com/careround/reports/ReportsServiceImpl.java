package com.careround.reports;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Escalation;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.Round;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientRoundReviewRepository;
import com.careround.patient.repository.RoundRepository;
import com.careround.reports.dto.ChartSeriesResponse;
import com.careround.reports.dto.RoundHistoryItemResponse;
import com.careround.shared.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService {

    private static final List<PatientStatus> ACTIVE_PATIENT_STATUSES =
            List.of(PatientStatus.ADMITTED, PatientStatus.STABLE, PatientStatus.DETERIORATING, PatientStatus.DISCHARGE_READY);
    private static final List<TaskStatus> OPEN_TASK_STATUSES =
            List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.OVERDUE);
    private static final List<EscalationStatus> OPEN_ESCALATION_STATUSES =
            List.of(EscalationStatus.OPEN, EscalationStatus.ACKNOWLEDGED);

    private final CareTaskRepository careTaskRepository;
    private final PatientRepository patientRepository;
    private final RoundRepository roundRepository;
    private final PatientRoundReviewRepository reviewRepository;
    private final WardRepository wardRepository;
    private final EscalationRepository escalationRepository;
    private final ShiftRepository shiftRepository;

    @Override
    @Transactional(readOnly = true)
    public ChartSeriesResponse taskCompletion(String hospitalId, String wardId, LocalDate from, LocalDate to) {
        DateRange range = validateAndBuildRange(hospitalId, wardId, from, to);
        List<CareTask> tasks = careTaskRepository.findReportTasks(
                hospitalId, wardId, TaskStatus.COMPLETED, range.start(), range.end());
        return byDay(range, tasks, task -> task.getCompletedAt() == null ? null : task.getCompletedAt().toLocalDate());
    }

    @Override
    @Transactional(readOnly = true)
    public ChartSeriesResponse overdueTasks(String hospitalId, String wardId, LocalDate from, LocalDate to) {
        DateRange range = validateAndBuildRange(hospitalId, wardId, from, to);
        List<CareTask> tasks = careTaskRepository.findOverdueReportTasks(
                hospitalId,
                wardId,
                TaskStatus.OVERDUE,
                List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS),
                range.start(),
                range.end());
        return byDay(range, tasks, task -> task.getWindowEnd() == null ? null : task.getWindowEnd().toLocalDate());
    }

    @Override
    @Transactional(readOnly = true)
    public ChartSeriesResponse patientFlow(String hospitalId, String wardId, LocalDate from, LocalDate to) {
        DateRange range = validateAndBuildRange(hospitalId, wardId, from, to);
        List<Patient> patients = patientRepository.findAdmissionsForReport(hospitalId, wardId, range.start(), range.end());
        return byDay(range, patients, patient -> patient.getAdmissionDate().toLocalDate());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> wardSummary(String hospitalId) {
        List<Ward> wards = wardRepository.findAllByHospitalId(hospitalId);
        List<String> wardIds = wards.stream().map(Ward::getId).toList();
        if (wardIds.isEmpty()) {
            return Map.of("wards", List.of(), "totals", Map.of(
                    "wards", 0,
                    "activePatients", 0,
                    "openTasks", 0,
                    "openEscalations", 0,
                    "activeShifts", 0));
        }

        LocalDateTime now = LocalDateTime.now();
        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdInAndStatusInOrderByWardIdAscNewsScoreDescAdmissionDateAsc(
                        hospitalId, wardIds, ACTIVE_PATIENT_STATUSES);
        List<CareTask> tasks = careTaskRepository
                .findAllByHospitalIdAndWardIdInAndStatusInOrderByWindowEndAsc(
                        hospitalId, wardIds, OPEN_TASK_STATUSES);
        List<Shift> shifts = shiftRepository
                .findAllByWardIdInAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByWardIdAscStartTimeDesc(
                        wardIds, ShiftStatus.ACTIVE, now, now);
        List<String> patientIds = patients.stream().map(Patient::getId).toList();
        List<Escalation> escalations = patientIds.isEmpty()
                ? List.of()
                : escalationRepository.findAllByPatientIdInAndStatusInOrderBySeverityDescCreatedAtAsc(
                        patientIds, OPEN_ESCALATION_STATUSES);

        List<Map<String, Object>> wardSummaries = wards.stream()
                .map(ward -> {
                    long activePatients = patients.stream().filter(patient -> ward.getId().equals(patient.getWardId())).count();
                    long openTasks = tasks.stream().filter(task -> ward.getId().equals(task.getWardId())).count();
                    long openEscalations = escalations.stream()
                            .filter(escalation -> patients.stream()
                                    .anyMatch(patient -> patient.getId().equals(escalation.getPatientId())
                                            && ward.getId().equals(patient.getWardId())))
                            .count();
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("wardId", ward.getId());
                    summary.put("name", ward.getName());
                    summary.put("specialty", ward.getSpecialty());
                    summary.put("totalBeds", ward.getTotalBeds());
                    summary.put("occupiedBeds", activePatients);
                    summary.put("availableBeds", Math.max(ward.getTotalBeds() - activePatients, 0));
                    summary.put("openTasks", openTasks);
                    summary.put("overdueTasks", tasks.stream()
                            .filter(task -> ward.getId().equals(task.getWardId()))
                            .filter(this::isOverdue)
                            .count());
                    summary.put("openEscalations", openEscalations);
                    return summary;
                })
                .toList();

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("wards", wards.size());
        totals.put("activePatients", patients.size());
        totals.put("openTasks", tasks.size());
        totals.put("openEscalations", escalations.size());
        totals.put("activeShifts", shifts.size());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", now);
        response.put("wards", wardSummaries);
        response.put("totals", totals);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundHistoryItemResponse> roundHistory(String hospitalId, String wardId, LocalDate from, LocalDate to) {
        DateRange range = validateAndBuildRange(hospitalId, wardId, from, to);
        return roundRepository.findRoundsForReport(hospitalId, wardId, range.start(), range.end()).stream()
                .map(round -> new RoundHistoryItemResponse(
                        round.getId(),
                        round.getWardId(),
                        round.getRoundType(),
                        round.getStatus(),
                        round.getScheduledTime(),
                        round.getStartedAt(),
                        round.getCompletedAt(),
                        durationMinutes(round),
                        reviewRepository.countByRoundId(round.getId()),
                        round.getLeadDoctorId()))
                .toList();
    }

    private DateRange validateAndBuildRange(String hospitalId, String wardId, LocalDate from, LocalDate to) {
        if (wardId != null && !wardId.isBlank()) {
            wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                    .orElseThrow(() -> new AccessDeniedException("Ward does not belong to this hospital"));
        }
        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        LocalDate effectiveFrom = from == null ? effectiveTo.minusDays(6) : from;
        return new DateRange(effectiveFrom, effectiveTo, effectiveFrom.atStartOfDay(), effectiveTo.atTime(LocalTime.MAX));
    }

    private <T> ChartSeriesResponse byDay(DateRange range, List<T> items, Function<T, LocalDate> dateExtractor) {
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        LocalDate cursor = range.from();
        while (!cursor.isAfter(range.to())) {
            counts.put(cursor, 0L);
            cursor = cursor.plusDays(1);
        }

        items.stream()
                .map(dateExtractor)
                .filter(date -> date != null && counts.containsKey(date))
                .forEach(date -> counts.computeIfPresent(date, (ignored, count) -> count + 1));

        return new ChartSeriesResponse(
                counts.keySet().stream().map(LocalDate::toString).toList(),
                counts.values().stream().toList());
    }

    private Long durationMinutes(Round round) {
        if (round.getStartedAt() == null || round.getCompletedAt() == null) {
            return null;
        }
        return Duration.between(round.getStartedAt(), round.getCompletedAt()).toMinutes();
    }

    private boolean isOverdue(CareTask task) {
        return task.getStatus() == TaskStatus.OVERDUE
                || (task.getWindowEnd() != null
                && task.getWindowEnd().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.CANCELLED);
    }

    private record DateRange(LocalDate from, LocalDate to, LocalDateTime start, LocalDateTime end) {}
}
