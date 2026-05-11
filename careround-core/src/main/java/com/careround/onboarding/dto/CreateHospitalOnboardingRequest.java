package com.careround.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHospitalOnboardingRequest(
        @NotBlank @Size(min = 2, max = 255) String hospitalName,
        @NotBlank @Size(max = 120) String countryOrRegion,
        @NotBlank @Email String contactEmail,
        @Size(max = 50) String contactPhone,
        @NotBlank @Size(max = 80) String hospitalType,
        @Size(max = 40) String estimatedInpatientBeds,
        @NotBlank @Size(min = 10, max = 5000) String primaryNeed
) {}
