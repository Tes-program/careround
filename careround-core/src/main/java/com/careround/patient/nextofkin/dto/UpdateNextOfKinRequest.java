package com.careround.patient.nextofkin.dto;

import com.careround.patient.enums.ContactMethod;

public record UpdateNextOfKinRequest(
        String name,
        String relationship,
        String phone,
        String email,
        ContactMethod preferredContactMethod,
        Boolean isEmergencyContact,
        Boolean notificationConsent
) {}
