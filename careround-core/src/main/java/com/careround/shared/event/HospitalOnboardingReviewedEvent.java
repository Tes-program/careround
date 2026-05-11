package com.careround.shared.event;

public record HospitalOnboardingReviewedEvent(
        String hospitalId,
        String requestId,
        String status,
        String reviewedByUserId,
        String correlationId
) {}
