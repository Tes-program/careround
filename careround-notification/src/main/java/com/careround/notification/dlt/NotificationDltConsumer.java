package com.careround.notification.dlt;

import com.careround.notification.dlt.entity.FailedNotification;
import com.careround.notification.dlt.repository.FailedNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDltConsumer {

    private final FailedNotificationRepository failedNotificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topicPattern = "careround\\..*\\.DLT",
                   groupId = "careround-notification-dlt")
    @Transactional
    public void handleDlt(ConsumerRecord<String, String> record) {
        log.warn("DLT message received: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());

        FailedNotification failed = new FailedNotification();
        failed.setEventType(record.topic().replace(".DLT", ""));
        failed.setTopic(record.topic());
        failed.setPayload(record.value());
        failed.setErrorMessage("Sent to DLT after max retries");
        failed.setFailedAt(LocalDateTime.now());

        Map<?, ?> payload = readPayload(record.value());
        failed.setHospitalId(asString(payload.get("hospitalId")));
        failed.setCorrelationId(asString(payload.get("correlationId")));

        failedNotificationRepository.save(failed);
    }

    private Map<?, ?> readPayload(String value) {
        try {
            return value != null ? objectMapper.readValue(value, Map.class) : Map.of();
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private String asString(Object value) {
        return value instanceof String string ? string : null;
    }
}
