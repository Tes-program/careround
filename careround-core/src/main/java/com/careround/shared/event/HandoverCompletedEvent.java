package com.careround.shared.event;

public record HandoverCompletedEvent(
        String hospitalId,
        String handoverId,
        String wardId,
        String outgoingShiftId,
        String incomingShiftId,
        String correlationId
) {}
