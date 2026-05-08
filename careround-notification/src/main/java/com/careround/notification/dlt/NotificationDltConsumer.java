package com.careround.notification.dlt;

import com.careround.notification.notification.Notification;
import com.careround.notification.notification.NotificationRepository;
import com.careround.notification.notification.NotificationStatus;
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

    private final NotificationRepository notificationRepository;

    @KafkaListener(topicPattern = "careround\\..*\\.DLT",
                   groupId = "careround-notification-dlt")
    @Transactional
    public void handleDlt(ConsumerRecord<String, String> record) {
        log.warn("DLT message received: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());

        var notification = new Notification();
        notification.setEventType(record.topic().replace(".DLT", ""));
        notification.setPayload(record.value() != null ? record.value() : null);
        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailureReason("Sent to DLT after max retries");
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}

