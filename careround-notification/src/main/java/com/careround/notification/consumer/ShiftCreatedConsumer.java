package com.careround.notification.consumer;

import com.careround.notification.client.CoreLookupClient;
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
public class ShiftCreatedConsumer {

    private static final String TOPIC = "careround.shift.created";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final CoreLookupClient coreLookupClient;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-shift-created-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            ShiftCreatedEvent event = objectMapper.readValue(payload, ShiftCreatedEvent.class);
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }

            coreLookupClient.findWardSupervisorId(event.wardId()).ifPresentOrElse(supervisorId ->
                    notificationService.persistAndSend(NotificationFactory.create(
                            event.hospitalId(), supervisorId, "USER", "EMAIL",
                            "New Shift Created - " + event.shiftType(),
                            "A new " + event.shiftType() + " shift has been created for your ward starting "
                                    + event.startTime() + ". Please assign staff before the shift begins.",
                            TOPIC, event.correlationId(), payload)),
                    () -> log.warn("action=SHIFT_CREATED_NOTIFICATION_SKIPPED correlationId={} wardId={}",
                            event.correlationId(), event.wardId()));

            log.info("action=SHIFT_CREATED_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=SHIFT_CREATED_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record ShiftCreatedEvent(String hospitalId, String shiftId, String wardId, String shiftType,
                             String startTime, String endTime, String correlationId) {
    }
}
