package com.careround.shared.service;

import com.careround.shared.event.OutboxEvent;
import com.careround.shared.event.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publish(String eventType, Object payload, String hospitalId) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            var event = new OutboxEvent();
            event.setEventType(eventType);
            event.setPayload(json);
            event.setHospitalId(hospitalId);
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event [type={}]: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }
}
