package com.careround.hospital.shift.dto;

import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.enums.ShiftType;

import java.time.LocalDateTime;

public record ShiftResponse(
        String id,
        String wardId,
        String shiftScheduleId,
        ShiftType type,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String leadDoctorId,
        String nurseInChargeId,
        ShiftStatus status,
        LocalDateTime assignedAt,
        LocalDateTime createdAt
) {}
