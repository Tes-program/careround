package com.careround.shared.event;

public record UserActivationRequestedEvent(
        String hospitalId,
        String userId,
        String email,
        String activationUrl,
        String correlationId
) {}
