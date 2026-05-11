package com.careround.patient.round.dto;

import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;

import java.time.LocalDateTime;

public record RoundResponse(
        String id,
        String hospitalId,
        String wardId,
        String medicalTeamId,
        String shiftId,
        RoundType roundType,
        String leadDoctorId,
        RoundStatus status,
        LocalDateTime scheduledTime,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String teamMembers,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
