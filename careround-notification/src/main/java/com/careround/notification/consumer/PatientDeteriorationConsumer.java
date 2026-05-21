package com.careround.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PatientDeteriorationConsumer {

    private static final String TOPIC = "careround.patient.deterioration";

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-patient-deterioration-group")
    public void listen(String payload) {
        log.info("action=PATIENT_DETERIORATION_RECEIVED topic={} payload={}", TOPIC, payload);
    }
}
