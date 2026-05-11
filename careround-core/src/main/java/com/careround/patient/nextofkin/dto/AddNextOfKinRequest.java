package com.careround.patient.nextofkin.dto;

import com.careround.patient.enums.ContactMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddNextOfKinRequest(
        @NotBlank String name,
        @NotBlank String relationship,
        @NotBlank String phone,
        String email,
        @NotNull ContactMethod preferredContactMethod,
        boolean isEmergencyContact,
        boolean notificationConsent
) {}
