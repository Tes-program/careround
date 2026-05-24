package com.careround.shared.event;

public record PatientAdmittedEvent(
        String eventId,
        String hospitalId,
        String patientId,
        String wardId,
        String correlationId
) {}
