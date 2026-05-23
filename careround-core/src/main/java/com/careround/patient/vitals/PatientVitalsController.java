package com.careround.patient.vitals;

import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients/{patientId}/vitals")
@RequiredArgsConstructor
@Tag(name = "Patient Vitals", description = "Patient vital sign recording and retrieval")
public class PatientVitalsController {

    private final PatientVitalsService patientVitalsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    @Operation(summary = "Record patient vitals", description = "Records a new set of vital signs for a patient.")
    public ResponseEntity<ApiResponse<VitalsResponse>> recordVitals(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String patientId,
            @Valid @RequestBody RecordVitalsRequest request) {
        VitalsResponse response = patientVitalsService.recordVitals(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Vitals recorded", response));
    }

    @GetMapping
    @Operation(summary = "List vitals history", description = "Returns recent vital sign entries for a patient, newest first.")
    public ResponseEntity<ApiResponse<List<VitalsResponse>>> getVitalsHistory(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String patientId,
            @Parameter(description = "Maximum number of entries to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(patientVitalsService.getVitalsHistory(patientId, limit)));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest vitals", description = "Returns the most recent vital sign entry for a patient.")
    public ResponseEntity<ApiResponse<VitalsResponse>> getLatestVitals(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientVitalsService.getLatestVitals(patientId)));
    }
}
