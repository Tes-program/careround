package com.careround.patient.patient;

import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient admission, lookup, ward list, and status updates")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admit patient", description = "Admits a patient into a ward.")
    public ResponseEntity<ApiResponse<PatientResponse>> admitPatient(
            @Valid @RequestBody AdmitPatientRequest request) {
        PatientResponse response = patientService.admitPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Patient admitted", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient", description = "Returns patient details by id.")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(
            @Parameter(description = "Patient id") @PathVariable("id") String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatient(patientId)));
    }

    @GetMapping("/ward/{wardId}")
    @Operation(summary = "List ward patients", description = "Returns admitted patients in a ward.")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getPatientsByWard(
            @PathVariable String wardId) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatientsByWard(wardId)));
    }

    @PatchMapping("/{patientId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Update patient status", description = "Updates a patient's admission status.")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatientStatus(
            @PathVariable String patientId,
            @Valid @RequestBody UpdatePatientStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", patientService.updatePatientStatus(patientId, request)));
    }
}
