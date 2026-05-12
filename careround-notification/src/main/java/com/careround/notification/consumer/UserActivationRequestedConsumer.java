package com.careround.notification.consumer;

import com.careround.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivationRequestedConsumer {

    private static final String TOPIC = "careround.user.activation_requested";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-user-activation-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            UserActivationRequestedEvent event = objectMapper.readValue(payload, UserActivationRequestedEvent.class);
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }
            notificationService.persistAndSend(NotificationFactory.create(
                    event.hospitalId(),
                    event.userId(),
                    "USER",
                    "EMAIL",
                    "Activate your CareRound account",
                    "Your hospital account is ready. Activate it here: " + event.activationUrl(),
                    TOPIC,
                    event.correlationId(),
                    payload));
            log.info("action=USER_ACTIVATION_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=USER_ACTIVATION_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record UserActivationRequestedEvent(String hospitalId, String userId, String email,
                                        String activationUrl, String correlationId) {
    }
}
