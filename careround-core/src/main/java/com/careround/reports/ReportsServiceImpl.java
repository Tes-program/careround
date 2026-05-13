package com.careround.reports;

import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.Round;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
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

    private final CareTaskRepository careTaskRepository;
    private final PatientRepository patientRepository;
    private final RoundRepository roundRepository;
    private final PatientRoundReviewRepository reviewRepository;
    private final WardRepository wardRepository;

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

    private record DateRange(LocalDate from, LocalDate to, LocalDateTime start, LocalDateTime end) {}
}
