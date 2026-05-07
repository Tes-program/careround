package com.careround.shared.event.events;

public record InviteExpiredEvent(String inviteId, String medicalTeamId, String invitedUserId, String hospitalId, String correlationId) {}
