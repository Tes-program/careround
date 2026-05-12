package com.careround.notification.consumer;

import com.careround.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CareTaskWorkloadConflictConsumer {

    private static final String TOPIC = "careround.care_task.workload_conflict";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-care-task-workload-conflict-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            CareTaskWorkloadConflictEvent event = objectMapper.readValue(payload, CareTaskWorkloadConflictEvent.class);
            if (!StringUtils.hasText(event.wardSupervisorId())) {
                log.warn("action=CARE_TASK_WORKLOAD_CONFLICT_NOTIFICATION_SKIPPED taskId={} wardId={}",
                        event.taskId(), event.wardId());
                return;
            }
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }
            notificationService.persistAndSend(NotificationFactory.create(
                    event.hospitalId(),
                    event.wardSupervisorId(),
                    "USER",
                    "EMAIL",
                    "Care task workload conflict",
                    "A care task was assigned despite a nurse workload conflict. Task: "
                            + event.taskId() + ". Reason: " + event.reason(),
                    TOPIC,
                    event.correlationId(),
                    payload));
            log.info("action=CARE_TASK_WORKLOAD_CONFLICT_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=CARE_TASK_WORKLOAD_CONFLICT_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record CareTaskWorkloadConflictEvent(
            String hospitalId,
            String taskId,
            String wardId,
            String patientId,
            String assignedNurseId,
            String wardSupervisorId,
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            String reason,
            String correlationId
    ) {}
}
