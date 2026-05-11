package com.careround.shared.event;

import com.careround.patient.enums.RoundType;

import java.time.LocalDateTime;

public record RoundCompletedEvent(
        String hospitalId,
        String roundId,
        String wardId,
        String medicalTeamId,
        String shiftId,
        RoundType roundType,
        String leadDoctorId,
        LocalDateTime completedAt,
        String correlationId
) {}
