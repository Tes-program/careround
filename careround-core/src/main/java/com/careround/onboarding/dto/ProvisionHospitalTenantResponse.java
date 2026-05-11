package com.careround.onboarding.dto;

import com.careround.onboarding.entity.HospitalOnboardingStatus;

public record ProvisionHospitalTenantResponse(
        String requestId,
        String hospitalId,
        String adminUserId,
        HospitalOnboardingStatus status
) {}
