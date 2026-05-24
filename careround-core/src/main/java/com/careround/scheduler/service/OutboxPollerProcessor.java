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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPollerProcessor {

    private static final Set<String> KNOWN_TOPICS = Set.of(
            "patient-admitted",
            "patient-discharged",
            "patient-updated",
            "prescription-confirmed",
            "prescription-discontinued",
            "clinical-note-saved",
            "medication-chart-created",
            "medication-task-completed",
            "medication-task-reminder",
            "medication-task-overdue",
            "vitals-recorded",
            "hospital-onboarding-requested",
            "hospital-onboarding-reviewed",
            "hospital-provisioned",
            "user-activation-requested",
            "manual-medication-added"
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

    public String resolveTopic(String eventType) {
        if (eventType == null) {
            return null;
        }
        return KNOWN_TOPICS.contains(eventType) ? eventType : null;
    }
}
