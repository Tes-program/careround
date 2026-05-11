package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.AmendNoteRequest;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clinical-notes")
@RequiredArgsConstructor
public class ClinicalNoteController {

    private final ClinicalNoteService clinicalNoteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR', 'NURSE', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> createNote(
            @Valid @RequestBody CreateClinicalNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Clinical note created", clinicalNoteService.createNote(request)));
    }

    @PatchMapping("/{noteId}/amend")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR', 'NURSE', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> amendNote(
            @PathVariable String noteId,
            @Valid @RequestBody AmendNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Clinical note amended",
                clinicalNoteService.amendNote(noteId, request)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getPatientNotes(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(clinicalNoteService.getPatientNotes(patientId)));
    }
}
