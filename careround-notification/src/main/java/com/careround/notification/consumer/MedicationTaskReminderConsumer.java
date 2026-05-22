package com.careround.notification.consumer;

import com.careround.notification.event.MedicationTaskReminderEvent;
import com.careround.notification.repository.NurseTokenRepository;
import com.careround.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationTaskReminderConsumer {

    private final NurseTokenRepository nurseTokenRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "medication-task-reminder",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        MedicationTaskReminderEvent event;
        try {
            event = objectMapper.readValue(record.value(), MedicationTaskReminderEvent.class);
        } catch (Exception ex) {
            log.error("action=MEDICATION_TASK_REMINDER_DESERIALIZE_FAILED offset={} message={}",
                    record.offset(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to deserialize medication-task-reminder event", ex);
        }

        Optional<String> token = nurseTokenRepository.findFcmToken(
                event.assignedNurseId(), event.hospitalId());

        if (token.isEmpty()) {
            log.info("action=FCM_SKIP_NO_TOKEN nurseId={} hospitalId={}",
                    event.assignedNurseId(), event.hospitalId());
            return;
        }

        try {
            String body = event.drugName() + " " + event.dose() + " is due soon";
            fcmService.send(token.get(), "Medication Reminder", body);
        } catch (Exception ex) {
            log.error("action=FCM_SEND_FAILED nurseId={} message={}",
                    event.assignedNurseId(), ex.getMessage(), ex);
            throw new RuntimeException("FCM send failed", ex);
        }
    }
}
