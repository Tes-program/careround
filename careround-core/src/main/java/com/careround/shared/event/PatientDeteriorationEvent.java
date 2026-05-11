package com.careround.shared.event;

import com.careround.patient.enums.EscalationSeverity;

public record PatientDeteriorationEvent(
        String hospitalId,
        String patientId,
        String wardId,
        int newsScore,
        EscalationSeverity severity,
        String escalationId,
        String assignedToId,
        String correlationId
) {}
