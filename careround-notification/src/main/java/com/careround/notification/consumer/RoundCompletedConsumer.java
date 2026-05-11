package com.careround.notification.consumer;

import com.careround.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoundCompletedConsumer {

    private static final String TOPIC = "careround.round.completed";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-round-completed-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            RoundCompletedEvent event = objectMapper.readValue(payload, RoundCompletedEvent.class);
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }
            if (!StringUtils.hasText(event.leadDoctorId())) {
                log.warn("action=ROUND_COMPLETED_NOTIFICATION_SKIPPED correlationId={} reason=missingLeadDoctor",
                        event.correlationId());
                return;
            }
            String roundType = event.roundType() != null ? event.roundType() : "scheduled";
            notificationService.persistAndSend(NotificationFactory.create(
                    event.hospitalId(),
                    event.leadDoctorId(),
                    "USER",
                    "EMAIL",
                    "Ward Round Completed - " + roundType,
                    "Your " + roundType + " ward round for ward " + event.wardId()
                            + " has been completed and recorded at " + event.completedAt() + ".",
                    TOPIC,
                    event.correlationId(),
                    payload));
            log.info("action=ROUND_COMPLETED_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=ROUND_COMPLETED_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record RoundCompletedEvent(String hospitalId, String roundId, String wardId, String medicalTeamId,
                               String roundType, String leadDoctorId, String completedAt, String correlationId) {
    }
}
