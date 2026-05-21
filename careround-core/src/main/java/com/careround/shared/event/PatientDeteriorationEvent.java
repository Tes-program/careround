package com.careround.shared.event;

public record PatientDeteriorationEvent(
        String hospitalId,
        String patientId,
        String wardId,
        int computedScore,
        String acuityColor,
        String correlationId
) {}
