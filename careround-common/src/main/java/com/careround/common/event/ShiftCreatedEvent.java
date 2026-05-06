package com.careround.common.event;

/** Published on: SHIFT_CREATED → careround.shift.created */
public record ShiftCreatedEvent(
        String shiftId,
        String wardId,
        String shiftType,
        String scheduleId,
        String hospitalId
) {}
