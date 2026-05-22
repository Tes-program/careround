package com.careround.shared.event;

import java.time.LocalDateTime;

public record PatientUpdatedEvent(
        String eventId,
        String patientId,
        String hospitalId,
        String wardId,
        String status,
        String correlationId,
        LocalDateTime timestamp
) {}
