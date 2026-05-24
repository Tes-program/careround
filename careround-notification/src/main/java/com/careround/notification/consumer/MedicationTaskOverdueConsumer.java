package com.careround.notification.consumer;

import com.careround.notification.event.MedicationTaskOverdueEvent;
import com.careround.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationTaskOverdueConsumer {

    private final NotificationService notificationService;
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

        log.debug("action=MEDICATION_TASK_OVERDUE_RECEIVED taskId={} patientId={} nurseId={}",
                event.taskId(), event.patientId(), event.assignedNurseId());

        notificationService.sendTaskOverdue(
                event.deviceToken(),
                event.taskId(),
                event.patientId(),
                event.patientName(),
                event.drugName(),
                event.dose(),
                (int) event.minutesOverdue()
        );
    }
}
