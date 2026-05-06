package com.careround.common.event;

/** Published on: ESCALATION_UNACKNOWLEDGED → careround.escalation.unacknowledged */
public record EscalationUnacknowledgedEvent(
        String escalationId,
        String patientId,
        String severity,
        String currentAssigneeId,
        String hospitalId
) {}
