package com.careround.shared.event;

import java.time.LocalDateTime;

public record MedicationTaskOverdueEvent(
        String eventId,
        String taskId,
        String patientId,
        String wardId,
        String hospitalId,
        String assignedNurseId,
        String drugName,
        String dose,
        LocalDateTime scheduledTime,
        long minutesOverdue,
        String correlationId,
        LocalDateTime timestamp
) {}
