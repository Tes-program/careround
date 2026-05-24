package com.careround.notification.consumer;

import com.careround.notification.dlt.entity.FailedNotification;
import com.careround.notification.dlt.repository.FailedNotificationRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDltConsumer {

    private final FailedNotificationRepository failedNotificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"medication-task-overdue.DLT", "medication-task-reminder.DLT"},
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        log.error("action=DLT_MESSAGE_RECEIVED topic={} partition={} offset={}",
                record.topic(), record.partition(), record.offset());

        String hospitalId = null;
        String correlationId = null;
        try {
            Map<String, Object> payload = objectMapper.readValue(record.value(), new TypeReference<>() {});
            hospitalId = (String) payload.get("hospitalId");
            correlationId = (String) payload.get("correlationId");
        } catch (Exception e) {
            log.warn("action=DLT_PAYLOAD_PARSE_FAILED topic={} offset={}", record.topic(), record.offset());
        }

        FailedNotification fn = new FailedNotification();
        fn.setEventType(record.topic().replace(".DLT", ""));
        fn.setTopic(record.topic());
        fn.setPayload(record.value());
        fn.setHospitalId(hospitalId);
        fn.setCorrelationId(correlationId);
        fn.setErrorMessage("Message moved to DLT after retries exhausted");
        fn.setFailedAt(LocalDateTime.now(ZoneOffset.UTC));
        failedNotificationRepository.save(fn);
    }
}
