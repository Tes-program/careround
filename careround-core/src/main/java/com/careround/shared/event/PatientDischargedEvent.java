package com.careround.shared.event;

import java.time.LocalDateTime;

public record PatientDischargedEvent(
        String hospitalId,
        String patientId,
        String wardId,
        LocalDateTime dischargedAt,
        String correlationId
) {
}
