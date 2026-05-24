package com.careround.hospital.hospital.dto;

import java.time.LocalDateTime;

public record SystemConfigResponse(
        String id,
        String hospitalId,
        int taskOverdueReminderMinutes,
        int taskEscalationMinutes,
        boolean pushNotificationsEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
