package com.careround.notification.event;

import java.time.LocalDateTime;

public record MedicationTaskReminderEvent(
        String eventId,
        String taskId,
        String patientId,
        String wardId,
        String hospitalId,
        String assignedNurseId,
        String drugName,
        String dose,
        LocalDateTime scheduledTime,
        String correlationId,
        LocalDateTime timestamp
) {}
