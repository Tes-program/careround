package com.careround.notification.consumer;

import com.careround.notification.client.CoreLookupClient;
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
public class TaskOverdueConsumer {

    private static final String TOPIC = "careround.task.overdue";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final CoreLookupClient coreLookupClient;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-task-overdue-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            TaskOverdueEvent event = objectMapper.readValue(payload, TaskOverdueEvent.class);
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }

            if (StringUtils.hasText(event.assignedToId())) {
                notificationService.persistAndSend(NotificationFactory.create(
                        event.hospitalId(), event.assignedToId(), "USER", "SMS", null,
                        "OVERDUE TASK: '" + event.title() + "' was due at " + event.windowEnd()
                                + ". Please complete or escalate immediately.",
                        TOPIC, event.correlationId(), payload));
            }

            coreLookupClient.findWardSupervisorId(event.wardId()).ifPresent(supervisorId ->
                    notificationService.persistAndSend(NotificationFactory.create(
                            event.hospitalId(), supervisorId, "USER", "EMAIL", "Overdue Task Alert",
                            "Task '" + event.title() + "' assigned in ward " + event.wardId()
                                    + " is overdue since " + event.windowEnd() + ".",
                            TOPIC, event.correlationId(), payload)));

            log.info("action=TASK_OVERDUE_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=TASK_OVERDUE_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record TaskOverdueEvent(String hospitalId, String taskId, String patientId, String wardId,
                            String assignedToId, String title, String windowEnd, String correlationId) {
    }
}
