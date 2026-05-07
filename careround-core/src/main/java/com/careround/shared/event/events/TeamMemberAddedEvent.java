package com.careround.shared.event.events;

public record TeamMemberAddedEvent(String medicalTeamId, String userId, String hospitalId, String correlationId) {}
