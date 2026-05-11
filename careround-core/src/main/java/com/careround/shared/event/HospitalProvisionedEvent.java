package com.careround.shared.event;

public record HospitalProvisionedEvent(
        String requestId,
        String hospitalId,
        String adminUserId,
        String correlationId
) {}
