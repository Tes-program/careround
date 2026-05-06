package com.careround.common.event;

/** Published on: HANDOVER_COMPLETED → careround.handover.completed */
public record HandoverCompletedEvent(
        String handoverId,
        String wardId,
        String incomingShiftId,
        String hospitalId
) {}
