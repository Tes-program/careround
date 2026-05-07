package com.careround.shared.event.events;

public record EscalationUnacknowledgedEvent(String escalationId, String patientId, com.careround.shared.enums.EscalationSeverity severity, String currentAssigneeId, String hospitalId, String correlationId) {}
