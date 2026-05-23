package com.careround.onboarding.dto;

import com.careround.onboarding.entity.HospitalOnboardingStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewHospitalOnboardingRequest(

        @NotNull
        HospitalOnboardingStatus status,

        String reviewNotes
) {}
