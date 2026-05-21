package com.careround.notification.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    void send_withValidArgs_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                notificationService.send("hosp-1", "user-1", "PUSH", "Patient deteriorating", "corr-1"));
    }

    @Test
    void send_withNullCorrelationId_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                notificationService.send("hosp-1", "user-1", "PUSH", "Test body", null));
    }
}
