package com.careround.notification.consumer;

import com.careround.notification.client.CoreLookupClient;
import com.careround.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientDeteriorationConsumer {

    private static final String TOPIC = "careround.patient.deterioration";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final CoreLookupClient coreLookupClient;
    private final NotificationIdempotencyGuard idempotencyGuard;

    @KafkaListener(topics = TOPIC, groupId = "careround-notification-patient-deterioration-group")
    @Transactional
    public void listen(String payload) {
        long start = System.currentTimeMillis();
        try {
            PatientDeteriorationEvent event = objectMapper.readValue(payload, PatientDeteriorationEvent.class);
            if (idempotencyGuard.alreadyProcessed(event.correlationId())) {
                return;
            }

            if (StringUtils.hasText(event.assignedToId())) {
                notificationService.persistAndSend(NotificationFactory.create(
                        event.hospitalId(), event.assignedToId(), "USER", "SMS", null,
                        "ALERT: Patient deterioration detected. NEWS2 score " + event.newsScore()
                                + ". Severity: " + event.severity()
                                + ". Please review escalation " + event.escalationId() + ".",
                        TOPIC, event.correlationId(), payload));
            }

            coreLookupClient.findWardSupervisorId(event.wardId()).ifPresentOrElse(supervisorId ->
                    notificationService.persistAndSend(NotificationFactory.create(
                            event.hospitalId(), supervisorId, "USER", "EMAIL", "Patient Deterioration Alert",
                            "Patient deterioration alert raised for ward " + event.wardId()
                                    + ". NEWS2 score: " + event.newsScore()
                                    + ". Severity: " + event.severity() + ".",
                            TOPIC, event.correlationId(), payload)),
                    () -> log.warn("action=PATIENT_DETERIORATION_SUPERVISOR_SKIPPED correlationId={} wardId={}",
                            event.correlationId(), event.wardId()));

            log.info("action=PATIENT_DETERIORATION_CONSUMED topic={} correlationId={} durationMs={}",
                    TOPIC, event.correlationId(), System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("action=PATIENT_DETERIORATION_CONSUME_FAILED topic={} durationMs={} message={}",
                    TOPIC, System.currentTimeMillis() - start, ex.getMessage(), ex);
        }
    }

    record PatientDeteriorationEvent(String hospitalId, String patientId, String wardId, int newsScore,
                                     String severity, String escalationId, String assignedToId,
                                     String correlationId) {
    }
}
