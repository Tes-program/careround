package com.careround.audit.consumer;

import com.careround.audit.entity.AuditLogEntry;
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
    void allTopics_writeAuditLogEntry() {
        ConsumerRecord<String, String> record = record("careround.patient.admitted");

        consumer.listen(record);

        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("careround.patient.admitted");
        assertThat(captor.getValue().getHospitalId()).isEqualTo("hosp-1");
    }

    @Test
    void duplicateCorrelationId_skipped() {
        when(auditLogRepository.existsByCorrelationId("corr-1")).thenReturn(true);

        consumer.listen(record("careround.round.completed"));

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void kafkaMetadataRecorded() {
        ConsumerRecord<String, String> record = record("careround.shift.created");

        consumer.listen(record);

        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getKafkaTopic()).isEqualTo("careround.shift.created");
        assertThat(captor.getValue().getKafkaPartition()).isEqualTo(2);
        assertThat(captor.getValue().getKafkaOffset()).isEqualTo(99L);
    }

    private ConsumerRecord<String, String> record(String topic) {
        return new ConsumerRecord<>(topic, 2, 99L, "hosp-1",
                "{\"hospitalId\":\"hosp-1\",\"correlationId\":\"corr-1\"}");
    }
}
