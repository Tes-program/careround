package com.careround.shared.event;

import java.time.LocalDateTime;

public record MedicationChartCreatedEvent(
        String eventId,
        String medicationChartId,
        String prescriptionId,
        String patientId,
        String wardId,
        String hospitalId,
        String correlationId,
        LocalDateTime timestamp
) {}
