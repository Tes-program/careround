package com.careround.onboarding.controller;

import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.dto.ProvisionHospitalTenantRequest;
import com.careround.onboarding.dto.ProvisionHospitalTenantResponse;
import com.careround.onboarding.dto.ReviewHospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import com.careround.onboarding.service.HospitalOnboardingService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding/hospital-requests")
@RequiredArgsConstructor
@Tag(name = "Hospital Onboarding", description = "Public hospital onboarding requests and platform-admin review/provisioning workflow")
public class HospitalOnboardingController {

    private final HospitalOnboardingService onboardingService;

    @PostMapping
    @Operation(
            summary = "Submit a hospital onboarding request",
            description = "Creates a public onboarding request for platform review. This does not create a live hospital tenant."
    )
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> submit(
            @Valid @RequestBody CreateHospitalOnboardingRequest request) {
        HospitalOnboardingResponse response = onboardingService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Onboarding request received", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
            summary = "List hospital onboarding requests",
            description = "Returns onboarding requests for platform admins, optionally filtered by review status."
    )
    public ResponseEntity<ApiResponse<List<HospitalOnboardingResponse>>> list(
            @Parameter(description = "Optional onboarding request status filter")
            @RequestParam(required = false) HospitalOnboardingStatus status,
            @Parameter(description = "Maximum number of requests to return")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Cursor for fetching the next page")
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.list(status, limit, cursor)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
            summary = "Get a hospital onboarding request",
            description = "Fetches one onboarding request by id for platform-admin review."
    )
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> get(
            @Parameter(description = "Onboarding request id") @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.get(id)));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
            summary = "Review a hospital onboarding request",
            description = "Moves a submitted request through the platform review workflow, such as contacted, approved, or rejected."
    )
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> review(
            @Parameter(description = "Onboarding request id") @PathVariable String id,
            @Valid @RequestBody ReviewHospitalOnboardingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Onboarding request reviewed",
                onboardingService.review(id, request)));
    }

    @PostMapping("/{id}/provision")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
            summary = "Provision an approved hospital tenant",
            description = "Creates the hospital tenant, default configuration, and first inactive tenant admin from an approved onboarding request."
    )
    public ResponseEntity<ApiResponse<ProvisionHospitalTenantResponse>> provision(
            @Parameter(description = "Approved onboarding request id") @PathVariable String id,
            @Valid @RequestBody ProvisionHospitalTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Hospital tenant provisioned",
                        onboardingService.provision(id, request)));
    }
}
