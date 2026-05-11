package com.careround.shared.event;

import java.time.LocalDateTime;

public record ShiftActivatedEvent(
        String hospitalId,
        String shiftId,
        String wardId,
        String leadDoctorId,
        String nurseInChargeId,
        String status,
        LocalDateTime assignedAt,
        String correlationId
) {}
