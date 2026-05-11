package com.careround.shared.event;

public record TaskOverdueEvent(
        String hospitalId,
        String taskId,
        String patientId,
        String wardId,
        String assignedToId,
        String title,
        String windowEnd,
        String correlationId
) {}
