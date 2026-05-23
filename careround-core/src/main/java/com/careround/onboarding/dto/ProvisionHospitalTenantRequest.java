package com.careround.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProvisionHospitalTenantRequest(

        @NotBlank
        String hospitalName,

        @NotBlank
        String address,

        @NotBlank @Email
        String contactEmail,

        String contactPhone,

        @NotBlank
        String adminFirstName,

        @NotBlank
        String adminLastName,

        @NotBlank @Email
        String adminEmail,

        @NotNull @Positive
        Integer newsAmberThreshold,

        @NotNull @Positive
        Integer newsRedThreshold,

        @NotNull @Positive
        Integer taskOverdueGraceMinutes,

        @NotNull
        Boolean roundNotificationsEnabled,

        @NotNull
        Boolean nokNotificationEnabled
) {}
