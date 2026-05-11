package com.careround.onboarding.dto;

import com.careround.onboarding.entity.HospitalOnboardingStatus;

import java.time.LocalDateTime;

public record HospitalOnboardingResponse(
        String id,
        String hospitalName,
        String countryOrRegion,
        String contactEmail,
        String contactPhone,
        String hospitalType,
        String estimatedInpatientBeds,
        String primaryNeed,
        HospitalOnboardingStatus status,
        String reviewNotes,
        String reviewedByUserId,
        LocalDateTime reviewedAt,
        String provisionedHospitalId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
