package com.careround.hospital.hospital.dto;

import jakarta.validation.constraints.Positive;

public record UpdateSystemConfigRequest(
        @Positive int acuityAmberThreshold,
        @Positive int acuityRedThreshold,
        @Positive int taskOverdueReminderMinutes,
        @Positive int taskEscalationMinutes,
        boolean pushNotificationsEnabled
) {}
