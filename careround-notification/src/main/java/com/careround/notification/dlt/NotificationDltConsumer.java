package com.careround.notification.dlt;

import com.careround.notification.dlt.entity.FailedNotification;
import com.careround.notification.dlt.repository.FailedNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDltConsumer {

    private final FailedNotificationRepository failedNotificationRepository;

    @KafkaListener(topicPattern = "careround\\..*\\.DLT",
                   groupId = "careround-notification-dlt")
    @Transactional
    public void handleDlt(ConsumerRecord<String, String> record) {
        log.warn("DLT message received: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());

        var failed = new FailedNotification();
        failed.setEventType(record.topic().replace(".DLT", ""));
        failed.setPayload(record.value() != null ? record.value().toString() : null);
        failed.setErrorMessage("Sent to DLT after max retries");
        failed.setFailedAt(LocalDateTime.now());
        failedNotificationRepository.save(failed);
    }
}
