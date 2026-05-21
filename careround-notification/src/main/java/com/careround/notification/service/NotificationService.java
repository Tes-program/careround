package com.careround.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void send(String hospitalId, String recipientId, String channel, String body, String correlationId) {
        log.info("action=NOTIFICATION_STUB hospitalId={} recipientId={} channel={} correlationId={}",
                hospitalId, recipientId, channel, correlationId);
    }
}
