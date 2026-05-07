package com.careround.shared.event.events;

public record HandoverCompletedEvent(String handoverId, String wardId, String incomingShiftId, String hospitalId, String correlationId) {}
