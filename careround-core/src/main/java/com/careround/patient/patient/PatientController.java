package com.careround.patient.patient;

import com.careround.patient.clinicalnote.ClinicalNoteService;
import com.careround.patient.clinicalnote.dto.ConfirmNoteResponse;
import com.careround.patient.clinicalnote.dto.ConfirmWardRoundNoteRequest;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient admission, lookup, ward list, and status updates")
public class PatientController {

    private final PatientService patientService;
    private final ClinicalNoteService clinicalNoteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admit patient", description = "Admits a patient into a ward.")
    public ResponseEntity<ApiResponse<PatientResponse>> admitPatient(
            @Valid @RequestBody AdmitPatientRequest request) {
        PatientResponse response = patientService.admitPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Patient admitted", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','SUPERVISOR')")
    @Operation(summary = "List all patients",
            description = "Returns patients for the hospital. Optional ?wardId= filters by ward; " +
                    "optional ?status= filters by status; optional ?q= searches by name (ignored when wardId is set).")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients(
            @Parameter(description = "Optional ward filter")
            @RequestParam(required = false) String wardId,
            @Parameter(description = "Optional status filter")
            @RequestParam(required = false) PatientStatus status,
            @Parameter(description = "Optional name search (first or last name, case-insensitive)")
            @RequestParam(required = false) String q) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        // When wardId is supplied, or when no name-search is needed, use the ward-aware path
        if (wardId != null || q == null || q.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(patientService.getPatients(hospitalId, wardId, status)));
        }
        // Fall back to name-search path (wardId not applicable here)
        return ResponseEntity.ok(ApiResponse.ok(patientService.getAllPatients(status, q)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient", description = "Returns patient details by id.")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("id") String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatient(patientId)));
    }

    @GetMapping("/ward/{wardId}")
    @Operation(summary = "List ward patients", description = "Returns admitted patients in a ward ordered by most recently admitted first. Optional ?q= filters by first or last name.")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getPatientsByWard(
            @Parameter(description = "Ward UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String wardId,
            @Parameter(description = "Optional name search (first or last name, case-insensitive)")
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatientsByWard(wardId, q)));
    }

    @PostMapping("/{patientId}/notes/confirm")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Confirm ward-round note with prescriptions",
            description = "Atomically saves a confirmed ward-round clinical note and its associated prescriptions " +
                    "under the patient resource. Triggers the async prescription → chart → task chain via Kafka.")
    public ResponseEntity<ApiResponse<ConfirmNoteResponse>> confirmNote(
            @Parameter(description = "Patient UUID") @PathVariable String patientId,
            @Valid @RequestBody ConfirmWardRoundNoteRequest request) {
        String doctorId = HospitalContextHolder.getUserId();
        String hospitalId = HospitalContextHolder.getHospitalId();
        ConfirmNoteResponse response = clinicalNoteService.confirmWardRoundNote(hospitalId, patientId, doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ward round note confirmed", response));
    }

    @PatchMapping("/{patientId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Update patient status", description = "Updates a patient's admission status.")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatientStatus(
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String patientId,
            @Valid @RequestBody UpdatePatientStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", patientService.updatePatientStatus(patientId, request)));
    }
}
