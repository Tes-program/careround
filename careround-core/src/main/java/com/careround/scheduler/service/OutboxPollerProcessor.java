package com.careround.scheduler.service;

import com.careround.shared.event.OutboxEvent;
import com.careround.shared.event.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPollerProcessor {

    private static final Map<String, String> EVENT_TOPIC_MAP = Map.ofEntries(
            Map.entry("PATIENT_ADMITTED", "careround.patient.admitted"),
            Map.entry("SHIFT_CREATED", "careround.shift.created"),
            Map.entry("SHIFT_ACTIVATED", "careround.shift.activated"),
            Map.entry("ROUND_COMPLETED", "careround.round.completed"),
            Map.entry("HANDOVER_COMPLETED", "careround.handover.completed"),
            Map.entry("TASK_OVERDUE", "careround.task.overdue"),
            Map.entry("PATIENT_DETERIORATION", "careround.patient.deterioration"),
            Map.entry("ESCALATION_UNACKNOWLEDGED", "careround.escalation.unacknowledged"),
            Map.entry("PATIENT_DISCHARGE_READY", "careround.patient.discharge-ready"),
            Map.entry("PATIENT_DISCHARGED", "careround.patient.discharged"),
            Map.entry("TEAM_INVITE_SENT", "careround.team.invite-sent"),
            Map.entry("TEAM_MEMBER_ADDED", "careround.team.member-added"),
            Map.entry("INVITE_EXPIRED", "careround.invite.expired")
    );

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public int pollAndPublishBatch() {
        int publishedCount = 0;

        for (OutboxEvent event : outboxEventRepository.findUnpublishedForUpdate(PageRequest.of(0, 100))) {
            String topic = resolveTopic(event.getEventType());
            if (topic == null) {
                log.error("action=OUTBOX_PUBLISH_UNMAPPED eventId={} eventType={}",
                        event.getId(), event.getEventType());
                continue;
            }

            String originalCorrelationId = MDC.get("correlationId");
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }

            try {
                objectMapper.readTree(event.getPayload());
                kafkaTemplate.send(topic, event.getHospitalId(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);
                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
                publishedCount++;
            } catch (TimeoutException ex) {
                log.error("action=OUTBOX_PUBLISH_FAILED eventId={} hospitalId={} topic={} message={}",
                        event.getId(), event.getHospitalId(), topic, ex.getMessage(), ex);
                throw new RuntimeException("Kafka send timed out for event " + event.getId(), ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("action=OUTBOX_PUBLISH_FAILED eventId={} topic={} correlationId={} message={}",
                        event.getId(), topic, event.getCorrelationId(), ex.getMessage(), ex);
            } catch (ExecutionException ex) {
                log.error("action=OUTBOX_PUBLISH_FAILED eventId={} hospitalId={} topic={} message={}",
                        event.getId(), event.getHospitalId(), topic, ex.getMessage(), ex);
                throw new RuntimeException("Kafka send failed for event " + event.getId(), ex);
            } catch (RuntimeException ex) {
                log.error("action=OUTBOX_PUBLISH_FAILED eventId={} topic={} correlationId={} message={}",
                        event.getId(), topic, event.getCorrelationId(), ex.getMessage(), ex);
            } catch (Exception ex) {
                log.error("action=OUTBOX_PUBLISH_FAILED eventId={} topic={} correlationId={} message={}",
                        event.getId(), topic, event.getCorrelationId(), ex.getMessage(), ex);
            } finally {
                if (originalCorrelationId != null) {
                    MDC.put("correlationId", originalCorrelationId);
                } else {
                    MDC.remove("correlationId");
                }
            }
        }

        return publishedCount;
    }

    String resolveTopic(String eventType) {
        if (eventType == null) {
            return null;
        }
        if (eventType.startsWith("careround.")) {
            return eventType;
        }
        return EVENT_TOPIC_MAP.get(eventType);
    }
}
