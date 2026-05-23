package com.careround.onboarding.controller;

import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.dto.ProvisionHospitalResponse;
import com.careround.onboarding.dto.ProvisionHospitalTenantRequest;
import com.careround.onboarding.dto.ReviewHospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import com.careround.onboarding.service.HospitalOnboardingService;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import java.net.URI;

@RestController
@RequestMapping("/api/v1/onboarding/hospital-requests")
@RequiredArgsConstructor
@Tag(name = "Hospital Onboarding", description = "Public hospital onboarding requests and platform-admin provisioning")
public class HospitalOnboardingController {

    private final HospitalOnboardingService onboardingService;

    @PostMapping
    @Operation(summary = "Submit a hospital onboarding request (public)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request submitted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate pending request")
    })
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> submit(
            @Valid @RequestBody CreateHospitalOnboardingRequest request) {
        HospitalOnboardingResponse response = onboardingService.submitRequest(request);
        return ResponseEntity
                .created(URI.create("/api/v1/onboarding/hospital-requests/" + response.id()))
                .body(ApiResponse.ok("Onboarding request submitted", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "List hospital onboarding requests")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request list returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    public ResponseEntity<ApiResponse<Page<HospitalOnboardingResponse>>> list(
            @RequestParam(required = false) HospitalOnboardingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<HospitalOnboardingResponse> result = onboardingService.listRequests(
                status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get a hospital onboarding request")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.getRequest(id)));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Review an onboarding request (CONTACTED / APPROVED / REJECTED)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request reviewed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Request not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> review(
            @PathVariable String id,
            @Valid @RequestBody ReviewHospitalOnboardingRequest request) {
        String reviewerUserId = HospitalContextHolder.getUserId();
        return ResponseEntity.ok(ApiResponse.ok("Request reviewed",
                onboardingService.reviewRequest(id, reviewerUserId, request)));
    }

    @PostMapping("/{id}/provision")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Provision a hospital tenant from an approved onboarding request")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hospital provisioned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Request not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already provisioned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<ProvisionHospitalResponse>> provision(
            @PathVariable String id,
            @Valid @RequestBody ProvisionHospitalTenantRequest request) {
        ProvisionHospitalResponse response = onboardingService.provisionTenant(id, request);
        return ResponseEntity
                .created(URI.create("/api/v1/hospitals/" + response.hospitalId()))
                .body(ApiResponse.ok("Hospital provisioned", response));
    }
}
