package com.careround.hospital.shiftschedule.dto;

import com.careround.hospital.enums.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CreateShiftScheduleRequest(
        String wardId,
        @NotNull ShiftType shiftType,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotBlank String daysOfWeek
) {}
