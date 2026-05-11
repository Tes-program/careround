package com.careround.patient.vitals;

import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
import com.careround.shared.dto.ApiResponse;
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
public class PatientVitalsController {

    private final PatientVitalsService patientVitalsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'JUNIOR_DOCTOR', 'REGISTRAR', 'CONSULTANT', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<VitalsResponse>> recordVitals(
            @PathVariable String patientId,
            @Valid @RequestBody RecordVitalsRequest request) {
        VitalsResponse response = patientVitalsService.recordVitals(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Vitals recorded", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VitalsResponse>>> getVitalsHistory(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(patientVitalsService.getVitalsHistory(patientId, limit)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<VitalsResponse>> getLatestVitals(
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientVitalsService.getLatestVitals(patientId)));
    }
}
