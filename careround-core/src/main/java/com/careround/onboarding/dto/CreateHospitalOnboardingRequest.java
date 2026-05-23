package com.careround.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHospitalOnboardingRequest(

        @NotBlank @Size(min = 2, max = 255)
        String hospitalName,

        @NotBlank
        String countryOrRegion,

        @NotBlank @Email
        String contactEmail,

        @Size(max = 50)
        String contactPhone,

        @NotBlank
        String hospitalType,

        String estimatedInpatientBeds,

        @NotBlank @Size(min = 10, max = 5000)
        String primaryNeed
) {}
