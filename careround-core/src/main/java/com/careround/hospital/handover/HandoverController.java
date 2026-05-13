package com.careround.hospital.handover;

import com.careround.hospital.handover.dto.AddPatientHandoverNoteRequest;
import com.careround.hospital.handover.dto.CompleteHandoverRequest;
import com.careround.hospital.handover.dto.HandoverResponse;
import com.careround.hospital.handover.dto.InitiateHandoverRequest;
import com.careround.hospital.handover.dto.PatientHandoverNoteResponse;
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
@RequestMapping("/api/v1/handovers")
@RequiredArgsConstructor
@Tag(name = "Handovers", description = "Shift handover initiation, completion, and patient handover notes")
public class HandoverController {

    private final HandoverService handoverService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'NURSE', 'WARD_SUPERVISOR')")
    @Operation(summary = "Initiate handover", description = "Starts a handover between outgoing and incoming shifts.")
    public ResponseEntity<ApiResponse<HandoverResponse>> initiateHandover(
            @Valid @RequestBody InitiateHandoverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Handover initiated", handoverService.initiateHandover(request)));
    }

    @PostMapping("/{handoverId}/patient-notes")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'NURSE', 'WARD_SUPERVISOR')")
    @Operation(summary = "Add patient handover note", description = "Adds a patient-specific note to a handover.")
    public ResponseEntity<ApiResponse<PatientHandoverNoteResponse>> addPatientHandoverNote(
            @PathVariable String handoverId,
            @Valid @RequestBody AddPatientHandoverNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Handover note added",
                        handoverService.addPatientHandoverNote(handoverId, request)));
    }

    @PostMapping("/{handoverId}/complete")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'NURSE', 'WARD_SUPERVISOR')")
    @Operation(summary = "Complete handover", description = "Completes an in-progress handover.")
    public ResponseEntity<ApiResponse<HandoverResponse>> completeHandover(
            @PathVariable String handoverId,
            @RequestBody(required = false) CompleteHandoverRequest request) {
        CompleteHandoverRequest req = request != null ? request : new CompleteHandoverRequest(null);
        return ResponseEntity.ok(ApiResponse.ok("Handover completed",
                handoverService.completeHandover(handoverId, req)));
    }

    @GetMapping("/ward/{wardId}")
    @Operation(summary = "List ward handovers", description = "Returns handovers for a ward.")
    public ResponseEntity<ApiResponse<List<HandoverResponse>>> getHandoversByWard(@PathVariable String wardId) {
        return ResponseEntity.ok(ApiResponse.ok(handoverService.getHandoversByWard(wardId)));
    }

    @GetMapping("/{handoverId}/patient-notes")
    @Operation(summary = "List handover notes", description = "Returns patient notes attached to a handover.")
    public ResponseEntity<ApiResponse<List<PatientHandoverNoteResponse>>> getHandoverNotes(
            @PathVariable String handoverId) {
        return ResponseEntity.ok(ApiResponse.ok(handoverService.getHandoverNotes(handoverId)));
    }
}
