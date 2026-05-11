package com.careround.patient.patient;

import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.MarkDischargeReadyRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.shared.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT', 'REGISTRAR', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<PatientResponse>> admitPatient(
            @Valid @RequestBody AdmitPatientRequest request) {
        PatientResponse response = patientService.admitPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Patient admitted", response));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatient(patientId)));
    }

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getPatientsByWard(
            @PathVariable String wardId) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatientsByWard(wardId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> searchPatients(
            @RequestParam("q") String query) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.searchPatients(query)));
    }

    @PatchMapping("/{patientId}/discharge-ready")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<ApiResponse<PatientResponse>> markDischargeReady(
            @PathVariable String patientId,
            @RequestBody MarkDischargeReadyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Discharge ready marked", patientService.markDischargeReady(patientId, request)));
    }

    @PatchMapping("/{patientId}/status")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatientStatus(
            @PathVariable String patientId,
            @Valid @RequestBody UpdatePatientStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", patientService.updatePatientStatus(patientId, request)));
    }
}
