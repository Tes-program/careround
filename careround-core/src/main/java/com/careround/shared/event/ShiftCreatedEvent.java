package com.careround.shared.event;

import com.careround.hospital.enums.ShiftType;

import java.time.LocalDateTime;

public record ShiftCreatedEvent(
        String hospitalId,
        String shiftId,
        String wardId,
        ShiftType shiftType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String correlationId
) {}
