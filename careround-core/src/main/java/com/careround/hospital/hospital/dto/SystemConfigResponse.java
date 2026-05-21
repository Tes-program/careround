package com.careround.hospital.hospital.dto;

public record SystemConfigResponse(
        String id,
        String hospitalId,
        int acuityAmberThreshold,
        int acuityRedThreshold,
        int taskOverdueReminderMinutes,
        int taskEscalationMinutes,
        boolean pushNotificationsEnabled
) {}
