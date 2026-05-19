package com.careround.scheduler.jobs;

import com.careround.scheduler.service.OutboxPollerProcessor;
import com.careround.shared.event.OutboxEvent;
import com.careround.shared.event.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerJobTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPollerProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OutboxPollerProcessor(outboxEventRepository, kafkaTemplate, new ObjectMapper());
    }

    @Test
    void noUnpublishedEvents_doesNothing() {
        when(outboxEventRepository.findUnpublishedForUpdate(any(Pageable.class))).thenReturn(List.of());

        int published = processor.pollAndPublishBatch();

        assertThat(published).isZero();
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void publishesUnpublishedEvents_marksPublished() {
        OutboxEvent event = unpublishedEvent("careround.round.completed");
        when(outboxEventRepository.findUnpublishedForUpdate(any(Pageable.class))).thenReturn(List.of(event));
        when(kafkaTemplate.send(eq("careround.round.completed"), eq("hosp-1"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        int published = processor.pollAndPublishBatch();

        assertThat(published).isEqualTo(1);
        assertThat(event.isPublished()).isTrue();
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    void kafkaFailure_doesNotMarkPublished() {
        OutboxEvent event = unpublishedEvent("careround.round.completed");
        when(outboxEventRepository.findUnpublishedForUpdate(any(Pageable.class))).thenReturn(List.of(event));
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        assertThatThrownBy(() -> processor.pollAndPublishBatch())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka send failed for event evt-1");
        assertThat(event.isPublished()).isFalse();
        assertThat(event.getPublishedAt()).isNull();
    }

    @Test
    void mapsEventTypeToCorrectTopic() {
        OutboxEvent event = unpublishedEvent("SHIFT_CREATED");
        when(outboxEventRepository.findUnpublishedForUpdate(any(Pageable.class))).thenReturn(List.of(event));
        when(kafkaTemplate.send(eq("careround.shift.created"), eq("hosp-1"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        processor.pollAndPublishBatch();

        verify(kafkaTemplate).send("careround.shift.created", "hosp-1", "{\"ok\":true}");
    }

    private OutboxEvent unpublishedEvent(String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setId("evt-1");
        event.setHospitalId("hosp-1");
        event.setEventType(eventType);
        event.setPayload("{\"ok\":true}");
        event.setCorrelationId("corr-1");
        return event;
    }
}
