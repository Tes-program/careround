package com.careround.shared.event;

import java.time.LocalDateTime;

public record TeamMemberAddedEvent(
        String hospitalId,
        String inviteId,
        String medicalTeamId,
        String userId,
        String invitedById,
        LocalDateTime acceptedAt,
        String correlationId
) {}
