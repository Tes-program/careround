package com.careround.shared.event.events;

public record TeamInviteSentEvent(String inviteId, String medicalTeamId, String invitedUserId, String invitedById, String hospitalId, String correlationId) {}
