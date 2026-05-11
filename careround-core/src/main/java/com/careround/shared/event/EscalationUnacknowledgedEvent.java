package com.careround.shared.event;

import com.careround.patient.enums.EscalationSeverity;

public record EscalationUnacknowledgedEvent(
        String hospitalId,
        String escalationId,
        String patientId,
        EscalationSeverity severity,
        String assignedToId,
        long minutesSinceCreation,
        String correlationId
) {}
