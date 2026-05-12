package com.careround.onboarding.controller;

import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.dto.ProvisionHospitalTenantRequest;
import com.careround.onboarding.dto.ProvisionHospitalTenantResponse;
import com.careround.onboarding.dto.ReviewHospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import com.careround.onboarding.service.HospitalOnboardingService;
import com.careround.shared.dto.ApiResponse;
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
public class HospitalOnboardingController {

    private final HospitalOnboardingService onboardingService;

    @PostMapping
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> submit(
            @Valid @RequestBody CreateHospitalOnboardingRequest request) {
        HospitalOnboardingResponse response = onboardingService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Onboarding request received", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<HospitalOnboardingResponse>>> list(
            @RequestParam(required = false) HospitalOnboardingStatus status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.list(status, limit, cursor)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.get(id)));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalOnboardingResponse>> review(
            @PathVariable String id,
            @Valid @RequestBody ReviewHospitalOnboardingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Onboarding request reviewed",
                onboardingService.review(id, request)));
    }

    @PostMapping("/{id}/provision")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ProvisionHospitalTenantResponse>> provision(
            @PathVariable String id,
            @Valid @RequestBody ProvisionHospitalTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Hospital tenant provisioned",
                        onboardingService.provision(id, request)));
    }
}
