package com.careround.notification.consumer;

import com.careround.notification.client.CoreLookupClient;
import com.careround.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientDischargedConsumer {

    private static final String TOPIC = "careround.patient.discharged";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final CoreLookupClient coreLookupClient;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-patient-discharged-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            PatientDischargedEvent event = objectMapper.readValue(payload, PatientDischargedEvent.class);
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }

            var contacts = coreLookupClient.findNextOfKin(event.patientId()).stream()
                    .filter(contact -> contact.notificationConsent())
                    .toList();
            if (contacts.isEmpty()) {
                log.info("action=PATIENT_DISCHARGED_NO_CONSENTING_NOK correlationId={} patientId={}",
                        event.correlationId(), event.patientId());
            }

            for (var contact : contacts) {
                String channel = "SMS".equalsIgnoreCase(contact.preferredContactMethod()) ? "SMS" : "EMAIL";
                notificationService.persistAndSend(NotificationFactory.create(
                        event.hospitalId(), contact.id(), "NOK", channel,
                        "EMAIL".equals(channel) ? "Discharge Notification" : null,
                        "We wanted to let you know that your family member has been discharged from our care. "
                                + "Please contact the ward if you have any questions.",
                        TOPIC, event.correlationId(), payload));
            }

            log.info("action=PATIENT_DISCHARGED_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=PATIENT_DISCHARGED_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record PatientDischargedEvent(String hospitalId, String patientId, String wardId, String dischargedAt,
                                  String correlationId) {
    }
}
