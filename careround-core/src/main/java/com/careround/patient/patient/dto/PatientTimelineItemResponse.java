package com.careround.patient.patient.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record PatientTimelineItemResponse(
        String id,
        String eventType,
        String title,
        String description,
        LocalDateTime occurredAt,
        String performedById,
        Map<String, Object> metadata
) {}
