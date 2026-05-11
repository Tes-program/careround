package com.careround.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProvisionHospitalTenantRequest(
        @NotBlank String hospitalName,
        String address,
        @NotBlank @Email String contactEmail,
        String contactPhone,
        @NotBlank String adminFirstName,
        @NotBlank String adminLastName,
        @NotBlank @Email String adminEmail,
        @Positive int newsAmberThreshold,
        @Positive int newsRedThreshold,
        @Positive int taskOverdueGraceMinutes,
        boolean roundNotificationsEnabled,
        boolean nokNotificationEnabled
) {}
