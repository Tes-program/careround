package com.careround.hospital.shiftschedule.dto;

import com.careround.hospital.enums.ShiftType;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ShiftScheduleResponse(
        String id,
        String hospitalId,
        String wardId,
        ShiftType shiftType,
        LocalTime startTime,
        LocalTime endTime,
        String daysOfWeek,
        boolean active,
        LocalDateTime createdAt
) {}
