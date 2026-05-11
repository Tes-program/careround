package com.careround.audit.consumer;

import com.careround.audit.entity.AuditLogEntry;
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
            "careround.patient.admitted",
            "careround.shift.created",
            "careround.shift.activated",
            "careround.round.completed",
            "careround.handover.completed",
            "careround.task.overdue",
            "careround.patient.deterioration",
            "careround.escalation.unacknowledged",
            "careround.patient.discharge-ready",
            "careround.patient.discharged",
            "careround.team.invite-sent",
            "careround.team.member-added",
            "careround.invite.expired",
            "careround.hospital.onboarding_requested",
            "careround.hospital.onboarding_reviewed",
            "careround.hospital.provisioned",
            "careround.user.activation_requested"
    }, groupId = "careround-audit-group")
    @Transactional
    public void listen(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> payload = objectMapper.readValue(record.value(), Map.class);
            String correlationId = asString(payload.get("correlationId"));
            if (StringUtils.hasText(correlationId) && auditLogRepository.existsByCorrelationId(correlationId)) {
                log.warn("action=AUDIT_DUPLICATE_SKIPPED topic={} correlationId={}", record.topic(), correlationId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            AuditLogEntry entry = new AuditLogEntry();
            entry.setHospitalId(asString(payload.get("hospitalId")));
            entry.setEventType(record.topic());
            entry.setPayload(record.value());
            entry.setCorrelationId(correlationId);
            entry.setKafkaTopic(record.topic());
            entry.setKafkaPartition(record.partition());
            entry.setKafkaOffset(record.offset());
            entry.setReceivedAt(now);
            entry.setProcessedAt(now);
            auditLogRepository.save(entry);

            log.info("action=AUDIT_LOG_WRITTEN topic={} correlationId={}", record.topic(), correlationId);
        } catch (Exception ex) {
            log.error("action=AUDIT_LOG_WRITE_FAILED topic={} partition={} offset={} message={}",
                    record.topic(), record.partition(), record.offset(), ex.getMessage(), ex);
        }
    }

    private String asString(Object value) {
        return value instanceof String string ? string : null;
    }
}
