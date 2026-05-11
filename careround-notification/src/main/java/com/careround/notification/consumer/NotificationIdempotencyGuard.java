package com.careround.notification.consumer;

import com.careround.notification.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationIdempotencyGuard {

    private final NotificationRepository notificationRepository;

    public boolean alreadyProcessed(String correlationId) {
        if (!StringUtils.hasText(correlationId)) {
            return false;
        }
        boolean exists = notificationRepository.existsByCorrelationId(correlationId);
        if (exists) {
            log.warn("action=NOTIFICATION_EVENT_DUPLICATE_SKIPPED correlationId={}", correlationId);
        }
        return exists;
    }
}
