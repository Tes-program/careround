package com.careround.hospital.hospital.dto;

public record SystemConfigResponse(
        String id,
        String hospitalId,
        int newsAmberThreshold,
        int newsRedThreshold,
        int taskOverdueGraceMinutes,
        boolean roundNotificationsEnabled,
        boolean nokNotificationEnabled
) {}
