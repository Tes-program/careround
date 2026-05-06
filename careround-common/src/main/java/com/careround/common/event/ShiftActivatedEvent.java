package com.careround.common.event;

/** Published on: SHIFT_ACTIVATED → careround.shift.activated */
public record ShiftActivatedEvent(
        String shiftId,
        String wardId,
        String leadDoctorId,
        String nurseInChargeId,
        String hospitalId
) {}
