package com.careround.shared.service;

import com.careround.shared.config.AppConfig;
import com.careround.shared.event.OutboxEvent;
import com.careround.shared.event.OutboxEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void publish_serializesJavaTimePayloadAndStoresCorrelationId() {
        OutboxService outboxService = new OutboxService(outboxEventRepository, new AppConfig().objectMapper());
        LocalDateTime occurredAt = LocalDateTime.of(2026, 5, 11, 13, 30);
        MDC.put("correlationId", "corr-123");

        outboxService.publish("SHIFT_CREATED", new Payload(occurredAt), "hosp-1");

        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());
        OutboxEvent savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getHospitalId()).isEqualTo("hosp-1");
        assertThat(savedEvent.getCorrelationId()).isEqualTo("corr-123");
        assertThat(savedEvent.getPayload()).contains("\"occurredAt\":\"2026-05-11T13:30:00\"");
    }

    private record Payload(LocalDateTime occurredAt) {
    }
}
