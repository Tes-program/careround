package com.careround.patient.nextofkin;

import com.careround.patient.nextofkin.dto.AddNextOfKinRequest;
import com.careround.patient.nextofkin.dto.NextOfKinResponse;
import com.careround.patient.nextofkin.dto.UpdateNextOfKinRequest;
import com.careround.patient.nextofkin.dto.UpdateNotificationConsentRequest;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients/{patientId}/next-of-kin")
@RequiredArgsConstructor
@Tag(name = "Next Of Kin", description = "Patient next-of-kin contacts and notification consent")
public class NextOfKinController {

    private final NextOfKinService nextOfKinService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'WARD_SUPERVISOR', 'CONSULTANT', 'REGISTRAR')")
    @Operation(summary = "Add next of kin", description = "Adds a next-of-kin contact to a patient.")
    public ResponseEntity<ApiResponse<NextOfKinResponse>> addNextOfKin(
            @PathVariable String patientId,
            @Valid @RequestBody AddNextOfKinRequest request) {
        NextOfKinResponse response = nextOfKinService.addNextOfKin(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Next of kin added", response));
    }

    @GetMapping
    @Operation(summary = "List next of kin", description = "Returns next-of-kin contacts for a patient.")
    public ResponseEntity<ApiResponse<List<NextOfKinResponse>>> getNextOfKin(
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(nextOfKinService.getNextOfKin(patientId)));
    }

    @PutMapping("/{nokId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'WARD_SUPERVISOR', 'CONSULTANT', 'REGISTRAR')")
    @Operation(summary = "Update next of kin", description = "Updates a patient next-of-kin contact.")
    public ResponseEntity<ApiResponse<NextOfKinResponse>> updateNextOfKin(
            @PathVariable String patientId,
            @PathVariable String nokId,
            @RequestBody UpdateNextOfKinRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Next of kin updated",
                nextOfKinService.updateNextOfKin(patientId, nokId, request)));
    }

    @DeleteMapping("/{nokId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARD_SUPERVISOR', 'CONSULTANT')")
    @Operation(summary = "Remove next of kin", description = "Removes a next-of-kin contact from a patient.")
    public ResponseEntity<ApiResponse<Void>> removeNextOfKin(
            @PathVariable String patientId,
            @PathVariable String nokId) {
        nextOfKinService.removeNextOfKin(patientId, nokId);
        return ResponseEntity.ok(ApiResponse.ok("Next of kin removed", null));
    }

    @PatchMapping("/{nokId}/consent")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'WARD_SUPERVISOR')")
    @Operation(summary = "Update notification consent", description = "Updates next-of-kin notification consent.")
    public ResponseEntity<ApiResponse<NextOfKinResponse>> updateNotificationConsent(
            @PathVariable String patientId,
            @PathVariable String nokId,
            @Valid @RequestBody UpdateNotificationConsentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Consent updated",
                nextOfKinService.updateNotificationConsent(patientId, nokId, request)));
    }
}
