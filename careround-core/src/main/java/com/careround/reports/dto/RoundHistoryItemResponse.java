package com.careround.reports.dto;

import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;

import java.time.LocalDateTime;

public record RoundHistoryItemResponse(
        String id,
        String wardId,
        RoundType roundType,
        RoundStatus status,
        LocalDateTime scheduledTime,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Long durationMinutes,
        long patientCount,
        String leadDoctorId
) {}
