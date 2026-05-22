package com.careround.shared.event;

import java.time.LocalDateTime;

public record MedicationTaskCompletedEvent(
        String eventId,
        String taskId,
        String medicationChartId,
        String patientId,
        String wardId,
        String hospitalId,
        String completedById,
        String actualDoseGiven,
        LocalDateTime completedAt,
        String correlationId,
        LocalDateTime timestamp
) {}
