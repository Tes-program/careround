package com.careround.shared.event.events;

public record TaskOverdueEvent(String careTaskId, String patientId, String wardId, String assignedToId, com.careround.shared.enums.TaskPriority priority, String hospitalId, String correlationId) {}
