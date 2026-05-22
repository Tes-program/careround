package com.careround.shared.event;

import java.time.LocalDateTime;

public record PrescriptionDiscontinuedEvent(
        String eventId,
        String prescriptionId,
        String patientId,
        String hospitalId,
        String discontinuedById,
        LocalDateTime discontinuedAt,
        String correlationId,
        LocalDateTime timestamp
) {}
