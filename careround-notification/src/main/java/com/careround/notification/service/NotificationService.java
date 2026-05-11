package com.careround.notification.service;

import com.careround.notification.notification.Notification;
import com.careround.notification.notification.NotificationRepository;
import com.careround.notification.notification.NotificationStatus;
import com.careround.notification.provider.EmailNotificationProvider;
import com.careround.notification.provider.SmsNotificationProvider;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationProvider emailNotificationProvider;
    private final SmsNotificationProvider smsNotificationProvider;

    @Transactional
    public void persistAndSend(Notification notification) {
        long start = System.currentTimeMillis();
        if (notification.getCorrelationId() != null
                && notificationRepository.existsByCorrelationIdAndRecipientIdAndChannel(
                notification.getCorrelationId(), notification.getRecipientId(), notification.getChannel())) {
            log.warn("action=NOTIFICATION_DUPLICATE_SKIPPED correlationId={} recipientId={} channel={}",
                    notification.getCorrelationId(), notification.getRecipientId(), notification.getChannel());
            return;
        }

        notification.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);

        try {
            if ("EMAIL".equalsIgnoreCase(saved.getChannel())) {
                emailNotificationProvider.send(saved);
            } else if ("SMS".equalsIgnoreCase(saved.getChannel())) {
                smsNotificationProvider.send(saved);
            } else {
                throw new IllegalArgumentException("Unsupported notification channel: " + saved.getChannel());
            }

            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
            saved.setFailureReason(null);
        } catch (CallNotPermittedException ex) {
            saved.setStatus(NotificationStatus.FAILED);
            saved.setFailureReason("Circuit breaker open");
        } catch (RuntimeException ex) {
            saved.setStatus(NotificationStatus.FAILED);
            saved.setFailureReason(ex.getMessage());
        }

        notificationRepository.save(saved);
        log.info("action=NOTIFICATION_PROCESSED topic={} correlationId={} durationMs={} status={}",
                saved.getEventType(), saved.getCorrelationId(), System.currentTimeMillis() - start, saved.getStatus());
    }
}
