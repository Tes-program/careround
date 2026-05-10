package com.careround.hospital.hospital.dto;

import jakarta.validation.constraints.Positive;

public record UpdateSystemConfigRequest(
        @Positive int newsAmberThreshold,
        @Positive int newsRedThreshold,
        @Positive int taskOverdueGraceMinutes,
        boolean roundNotificationsEnabled,
        boolean nokNotificationEnabled
) {}
