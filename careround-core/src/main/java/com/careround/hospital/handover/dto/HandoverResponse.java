package com.careround.hospital.handover.dto;

import com.careround.hospital.enums.HandoverStatus;

import java.time.LocalDateTime;

public record HandoverResponse(
        String id,
        String wardId,
        String outgoingShiftId,
        String incomingShiftId,
        String conductedById,
        HandoverStatus status,
        String generalNotes,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
