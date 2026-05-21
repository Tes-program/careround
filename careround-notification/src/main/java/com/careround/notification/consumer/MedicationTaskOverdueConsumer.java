package com.careround.notification.consumer;

import com.careround.notification.event.MedicationTaskOverdueEvent;
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
public class MedicationTaskOverdueConsumer {

    private final NurseTokenRepository nurseTokenRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "medication-task-overdue",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        MedicationTaskOverdueEvent event;
        try {
            event = objectMapper.readValue(record.value(), MedicationTaskOverdueEvent.class);
        } catch (Exception ex) {
            log.error("action=MEDICATION_TASK_OVERDUE_DESERIALIZE_FAILED offset={} message={}",
                    record.offset(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to deserialize medication-task-overdue event", ex);
        }

        Optional<String> token = nurseTokenRepository.findFcmToken(
                event.assignedNurseId(), event.hospitalId());

        if (token.isEmpty()) {
            log.info("action=FCM_SKIP_NO_TOKEN nurseId={} hospitalId={}",
                    event.assignedNurseId(), event.hospitalId());
            return;
        }

        try {
            String body = event.drugName() + " " + event.dose()
                    + " is " + event.minutesOverdue() + " minutes overdue";
            fcmService.send(token.get(), "Medication Overdue", body);
        } catch (Exception ex) {
            log.error("action=FCM_SEND_FAILED nurseId={} message={}",
                    event.assignedNurseId(), ex.getMessage(), ex);
        }
    }
}
