package com.careround.shared.event;

import java.time.LocalDateTime;

public record ClinicalNoteSavedEvent(
        String eventId,
        String noteId,
        String patientId,
        String hospitalId,
        String correlationId,
        LocalDateTime timestamp
) {}
