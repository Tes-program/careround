package com.careround.shared.event;

import java.time.LocalDateTime;

public record InviteExpiredEvent(
        String hospitalId,
        String inviteId,
        String medicalTeamId,
        String invitedUserId,
        String invitedById,
        LocalDateTime expiredAt,
        String correlationId
) {}
