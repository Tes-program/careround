package com.careround.hospital.hospital.dto;

public record SystemConfigResponse(
        String id,
        String hospitalId,
        int taskOverdueReminderMinutes,
        int taskEscalationMinutes,
        boolean pushNotificationsEnabled
) {}
