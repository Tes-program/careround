package com.careround.shared.event;

import com.careround.test.DataJpaH2Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaH2Test
class ProcessedEventRepositoryTest {

    @Autowired
    private ProcessedEventRepository repository;

    @Test
    void existsById_returnsFalseBeforeSave() {
        assertThat(repository.existsById("event-abc")).isFalse();
    }

    @Test
    void existsById_returnsTrueAfterSave() {
        ProcessedEvent event = new ProcessedEvent();
        event.setEventId("event-xyz");
        event.setProcessedAt(LocalDateTime.now());
        repository.save(event);

        assertThat(repository.existsById("event-xyz")).isTrue();
    }

    @Test
    void save_pkIsEventIdString() {
        ProcessedEvent event = new ProcessedEvent();
        event.setEventId("idempotency-key-123");
        event.setProcessedAt(LocalDateTime.now());
        ProcessedEvent saved = repository.save(event);

        assertThat(saved.getEventId()).isEqualTo("idempotency-key-123");
        assertThat(repository.findById("idempotency-key-123")).isPresent();
    }
}
