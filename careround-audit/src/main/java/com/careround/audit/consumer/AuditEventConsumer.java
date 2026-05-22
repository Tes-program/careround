package com.careround.audit.consumer;

import com.careround.audit.entity.AuditLog;
import com.careround.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {
            "patient-admitted",
            "patient-discharged",
            "patient-updated",
            "clinical-note-saved",
            "prescription-confirmed",
            "prescription-discontinued",
            "medication-chart-created",
            "medication-task-reminder",
            "medication-task-completed",
            "medication-task-overdue",
            "vitals-recorded"
    }, groupId = "careround-audit-group")
    @Transactional
    public void listen(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> payload = objectMapper.readValue(record.value(), Map.class);
            String eventId = asString(payload.get("eventId"));
            String correlationId = asString(payload.get("correlationId"));
            if (StringUtils.hasText(eventId) && auditLogRepository.existsByEventId(eventId)) {
                log.warn("action=AUDIT_DUPLICATE_SKIPPED topic={} eventId={}", record.topic(), eventId);
                return;
            }

            AuditLog entry = new AuditLog();
            entry.setEventId(eventId);
            entry.setHospitalId(asString(payload.get("hospitalId")));
            entry.setEventType(record.topic());
            entry.setPayload(record.value());
            entry.setCorrelationId(correlationId);
            entry.setReceivedAt(LocalDateTime.now());
            auditLogRepository.save(entry);

            log.info("action=AUDIT_LOG_WRITTEN topic={} eventId={}", record.topic(), eventId);
        } catch (Exception ex) {
            log.error("action=AUDIT_LOG_WRITE_FAILED topic={} partition={} offset={} message={}",
                    record.topic(), record.partition(), record.offset(), ex.getMessage(), ex);
        }
    }

    private String asString(Object value) {
        return value instanceof String string ? string : null;
    }
}
