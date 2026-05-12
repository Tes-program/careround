package com.careround.onboarding.dto;

import com.careround.onboarding.entity.HospitalOnboardingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewHospitalOnboardingRequest(
        @NotNull HospitalOnboardingStatus status,
        @Size(max = 5000) String reviewNotes
) {}
