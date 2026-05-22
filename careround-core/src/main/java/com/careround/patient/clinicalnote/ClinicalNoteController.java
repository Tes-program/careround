package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.ConfirmNoteRequest;
import com.careround.patient.clinicalnote.dto.ConfirmNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clinical-notes")
@RequiredArgsConstructor
@Tag(name = "Clinical Notes", description = "Clinical note creation and patient note history")
public class ClinicalNoteController {

    private final ClinicalNoteService clinicalNoteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Create clinical note", description = "Creates a clinical note for a patient.")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> createNote(
            @Valid @RequestBody CreateClinicalNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Clinical note created", clinicalNoteService.createNote(request)));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Confirm clinical note with prescriptions",
            description = "Atomically saves a confirmed clinical note and its associated prescriptions.")
    public ResponseEntity<ApiResponse<ConfirmNoteResponse>> confirmNote(
            @Valid @RequestBody ConfirmNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Clinical note confirmed", clinicalNoteService.confirm(request)));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "List patient clinical notes", description = "Returns clinical notes for a patient.")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getPatientNotes(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(clinicalNoteService.getPatientNotes(patientId)));
    }
}
