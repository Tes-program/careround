package com.careround.patient.medicationchart;

import com.careround.patient.medicationchart.dto.AddManualMedicationRequest;
import com.careround.patient.medicationchart.dto.MedicationChartResponse;
import com.careround.patient.medicationchart.dto.UpdateMedicationChartRequest;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Medication Chart", description = "Medication chart viewing and management")
public class MedicationChartController {

    private final MedicationChartService medicationChartService;

    @GetMapping("/api/v1/patients/{patientId}/medication-chart")
    @Operation(summary = "Get medication chart", description = "Returns all chart entries for a patient.")
    public ResponseEntity<ApiResponse<List<MedicationChartResponse>>> getChart(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(medicationChartService.getChartForPatient(patientId)));
    }

    @PutMapping("/api/v1/medication-charts/{chartId}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    @Operation(summary = "Update chart entry", description = "Updates nurse notes on a medication chart entry.")
    public ResponseEntity<ApiResponse<MedicationChartResponse>> updateChart(
            @Parameter(description = "Medication chart entry UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String chartId,
            @Valid @RequestBody UpdateMedicationChartRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Chart updated",
                medicationChartService.updateNurseNotes(chartId, request)));
    }

    @PostMapping("/api/v1/medication-charts/{patientId}/manual")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    @Operation(summary = "Add manual medication", description = "Manually adds a medication to the chart.")
    public ResponseEntity<ApiResponse<MedicationChartResponse>> addManual(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String patientId,
            @Valid @RequestBody AddManualMedicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Medication added",
                        medicationChartService.addManualMedication(patientId, request)));
    }

    @PutMapping("/api/v1/medication-charts/{chartId}/discontinue")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    @Operation(summary = "Discontinue chart entry", description = "Marks a chart entry and its pending tasks as discontinued.")
    public ResponseEntity<ApiResponse<MedicationChartResponse>> discontinue(
            @Parameter(description = "Medication chart entry UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String chartId) {
        return ResponseEntity.ok(ApiResponse.ok("Chart entry discontinued",
                medicationChartService.discontinue(chartId)));
    }
}
