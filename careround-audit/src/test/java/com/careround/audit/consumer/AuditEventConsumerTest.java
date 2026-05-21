package com.careround.audit.consumer;

import com.careround.audit.entity.AuditLog;
import com.careround.audit.repository.AuditLogRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventConsumerTest {

    @Mock private AuditLogRepository auditLogRepository;

    private AuditEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new AuditEventConsumer(auditLogRepository, new ObjectMapper());
    }

    @Test
    void listen_validRecord_writesAuditLog() {
        ConsumerRecord<String, String> record = record("careround.patient.admitted", "event-1");

        consumer.listen(record);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo("careround.patient.admitted");
        assertThat(saved.getHospitalId()).isEqualTo("hosp-1");
        assertThat(saved.getEventId()).isEqualTo("event-1");
        assertThat(saved.getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void listen_duplicateEventId_skipsProcessing() {
        when(auditLogRepository.existsByEventId("event-dup")).thenReturn(true);

        consumer.listen(record("careround.patient.discharged", "event-dup"));

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listen_malformedJson_doesNotThrow() {
        ConsumerRecord<String, String> bad = new ConsumerRecord<>(
                "careround.note.created", 0, 1L, "key", "not-json");

        consumer.listen(bad);

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private ConsumerRecord<String, String> record(String topic, String eventId) {
        String payload = String.format(
                "{\"eventId\":\"%s\",\"hospitalId\":\"hosp-1\",\"correlationId\":\"corr-1\"}", eventId);
        return new ConsumerRecord<>(topic, 2, 99L, "hosp-1", payload);
    }
}
