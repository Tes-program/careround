package com.careround.patient.handovernote;

import com.careround.patient.handovernote.dto.CreateHandoverNoteRequest;
import com.careround.patient.handovernote.dto.HandoverNoteResponse;
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
@RequestMapping("/api/v1/patients/{patientId}/handover-notes")
@RequiredArgsConstructor
@Tag(name = "Handover Notes", description = "Patient handover and nursing report notes")
public class HandoverNoteController {

    private final HandoverNoteService handoverNoteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    @Operation(summary = "Add handover note", description = "Adds a handover or nursing report note for a patient.")
    public ResponseEntity<ApiResponse<HandoverNoteResponse>> create(
            @PathVariable String patientId,
            @Valid @RequestBody CreateHandoverNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Handover note added",
                        handoverNoteService.create(patientId, request)));
    }

    @GetMapping
    @Operation(summary = "List handover notes", description = "Returns handover notes for a patient, newest first.")
    public ResponseEntity<ApiResponse<List<HandoverNoteResponse>>> list(
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(handoverNoteService.list(patientId)));
    }
}
