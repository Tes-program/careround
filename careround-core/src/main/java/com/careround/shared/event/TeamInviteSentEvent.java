package com.careround.shared.event;

import java.time.LocalDateTime;

public record TeamInviteSentEvent(
        String hospitalId,
        String inviteId,
        String medicalTeamId,
        String invitedUserId,
        String invitedById,
        LocalDateTime expiresAt,
        String correlationId
) {}
