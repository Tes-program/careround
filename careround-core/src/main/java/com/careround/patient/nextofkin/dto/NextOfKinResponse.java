package com.careround.patient.nextofkin.dto;

import com.careround.patient.enums.ContactMethod;

import java.time.LocalDateTime;

public record NextOfKinResponse(
        String id,
        String patientId,
        String name,
        String relationship,
        String phone,
        String email,
        ContactMethod preferredContactMethod,
        boolean isEmergencyContact,
        boolean notificationConsent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
