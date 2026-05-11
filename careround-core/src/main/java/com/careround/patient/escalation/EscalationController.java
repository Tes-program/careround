package com.careround.patient.escalation;

import com.careround.patient.escalation.dto.AcknowledgeEscalationRequest;
import com.careround.patient.escalation.dto.CreateEscalationRequest;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.patient.escalation.dto.ResolveEscalationRequest;
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
@RequestMapping("/api/v1/escalations")
@RequiredArgsConstructor
public class EscalationController {

    private final EscalationService escalationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'JUNIOR_DOCTOR', 'REGISTRAR', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<EscalationResponse>> createManualEscalation(
            @Valid @RequestBody CreateEscalationRequest request) {
        EscalationResponse response = escalationService.createManualEscalation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Escalation created", response));
    }

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<ApiResponse<List<EscalationResponse>>> getOpenEscalations(
            @PathVariable String wardId) {
        return ResponseEntity.ok(ApiResponse.ok(escalationService.getOpenEscalations(wardId)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<EscalationResponse>>> getEscalationsByPatient(
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(escalationService.getEscalationsByPatient(patientId)));
    }

    @PatchMapping("/{escalationId}/acknowledge")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'CONSULTANT')")
    public ResponseEntity<ApiResponse<EscalationResponse>> acknowledgeEscalation(
            @PathVariable String escalationId,
            @RequestBody AcknowledgeEscalationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Escalation acknowledged",
                escalationService.acknowledgeEscalation(escalationId, request)));
    }

    @PatchMapping("/{escalationId}/resolve")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'CONSULTANT')")
    public ResponseEntity<ApiResponse<EscalationResponse>> resolveEscalation(
            @PathVariable String escalationId,
            @Valid @RequestBody ResolveEscalationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Escalation resolved",
                escalationService.resolveEscalation(escalationId, request)));
    }
}
