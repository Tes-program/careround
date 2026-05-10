package com.careround.hospital.medicalteam.dto;

import com.careround.hospital.enums.InviteStatus;

import java.time.LocalDateTime;

public record InviteResponse(
        String id,
        String hospitalId,
        String medicalTeamId,
        String invitedUserId,
        String invitedById,
        InviteStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {}
