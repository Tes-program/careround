package com.careround.notification.consumer;

import com.careround.notification.notification.Notification;

import java.time.LocalDateTime;

final class NotificationFactory {

    private NotificationFactory() {
    }

    static Notification create(String hospitalId, String recipientId, String recipientType, String channel,
                               String subject, String body, String eventType, String correlationId, String payload) {
        Notification notification = new Notification();
        notification.setHospitalId(hospitalId);
        notification.setRecipientId(recipientId);
        notification.setRecipientType(recipientType);
        notification.setChannel(channel);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setEventType(eventType);
        notification.setCorrelationId(correlationId);
        notification.setPayload(payload);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
