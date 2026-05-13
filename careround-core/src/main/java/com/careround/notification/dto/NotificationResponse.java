package com.careround.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String type,
        String title,
        String body,
        String routeTarget,
        boolean read,
        LocalDateTime createdAt
) {}
