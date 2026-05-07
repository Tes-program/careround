package com.careround.shared.event.events;

public record RoundCompletedEvent(String roundId, String wardId, java.util.List<String> reviewedPatientIds, String hospitalId, String correlationId) {}
