package com.careround.patient.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Chronological patient timeline item covering admission, vitals, escalations, reviews, notes, and care tasks.")
public record PatientTimelineItemResponse(
        @Schema(description = "Source entity id for the event.")
        String id,
        @Schema(description = "Timeline event type, for example ADMISSION, VITALS, ESCALATION, ROUND_REVIEW, CLINICAL_NOTE, or CARE_TASK.")
        String type,
        @Schema(description = "Short display title for the event.")
        String title,
        @Schema(description = "Longer clinical or operational event description.")
        String description,
        @Schema(description = "When this event occurred.")
        LocalDateTime occurredAt,
        @Schema(description = "User id responsible for the event, when available.")
        String actorId,
        @Schema(description = "Event-specific structured metadata.")
        Map<String, Object> metadata
) {}
