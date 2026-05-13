package com.careround.reports;

import com.careround.reports.dto.ChartSeriesResponse;
import com.careround.reports.dto.RoundHistoryItemResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Operational reports for task, patient-flow, and round-history views")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/task-completion")
    @Operation(summary = "Task completion report", description = "Returns chart-ready daily completed care task counts.")
    public ResponseEntity<ApiResponse<ChartSeriesResponse>> taskCompletion(
            @Parameter(description = "Optional ward filter") @RequestParam(required = false) String wardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportsService.taskCompletion(HospitalContextHolder.getHospitalId(), wardId, from, to)));
    }

    @GetMapping("/overdue-tasks")
    @Operation(summary = "Overdue tasks report", description = "Returns chart-ready daily overdue care task counts.")
    public ResponseEntity<ApiResponse<ChartSeriesResponse>> overdueTasks(
            @RequestParam(required = false) String wardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportsService.overdueTasks(HospitalContextHolder.getHospitalId(), wardId, from, to)));
    }

    @GetMapping("/patient-flow")
    @Operation(summary = "Patient flow report", description = "Returns chart-ready daily admission counts for the selected date range.")
    public ResponseEntity<ApiResponse<ChartSeriesResponse>> patientFlow(
            @RequestParam(required = false) String wardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportsService.patientFlow(HospitalContextHolder.getHospitalId(), wardId, from, to)));
    }

    @GetMapping("/round-history")
    @Operation(summary = "Round history report", description = "Returns round status, duration, patient count, and lead doctor data for a date range.")
    public ResponseEntity<ApiResponse<List<RoundHistoryItemResponse>>> roundHistory(
            @RequestParam(required = false) String wardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportsService.roundHistory(HospitalContextHolder.getHospitalId(), wardId, from, to)));
    }
}
