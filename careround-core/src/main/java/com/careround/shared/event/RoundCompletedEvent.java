package com.careround.shared.event;

public record RoundCompletedEvent(
        String hospitalId,
        String roundId,
        String wardId,
        String medicalTeamId,
        String shiftId,
        String correlationId
) {}
