package com.careround.notification.client;

public record NextOfKinContact(
        String id,
        String preferredContactMethod,
        boolean notificationConsent
) {
}
