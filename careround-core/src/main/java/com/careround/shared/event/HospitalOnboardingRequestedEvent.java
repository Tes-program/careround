package com.careround.shared.event;

public record HospitalOnboardingRequestedEvent(
        String hospitalId,
        String requestId,
        String hospitalName,
        String contactEmail,
        String correlationId
) {}
