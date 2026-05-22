package com.careround.shared.event;

import java.time.LocalDateTime;

public record VitalsRecordedEvent(
        String eventId,
        String vitalsId,
        String patientId,
        String hospitalId,
        String recordedById,
        int vhiScore,
        String vhiStatus,
        String previousVhiStatus,
        LocalDateTime recordedAt,
        String correlationId,
        LocalDateTime timestamp
) {}
