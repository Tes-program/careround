package com.careround.scheduler.service;

import com.careround.patient.entity.Escalation;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.repository.EscalationRepository;
import com.careround.shared.event.EscalationUnacknowledgedEvent;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EscalationUnacknowledgedProcessor {

    private final EscalationRepository escalationRepository;
    private final OutboxService outboxService;

    @Transactional
    public int process(String correlationId) {
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(15);
        List<Escalation> escalations = escalationRepository.findAllByStatusAndCreatedAtBefore(
                EscalationStatus.OPEN,
                threshold
        );

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int processed = 0;
        for (Escalation escalation : escalations) {
            if (escalation.getStatus() != EscalationStatus.OPEN
                    || escalation.getCreatedAt() == null
                    || !escalation.getCreatedAt().isBefore(now.minusMinutes(15))) {
                continue;
            }
            publishReminder(escalation, correlationId);
            processed++;
        }

        return processed;
    }

    private void publishReminder(Escalation escalation, String correlationId) {
        long minutesSinceCreation = Duration.between(escalation.getCreatedAt(), LocalDateTime.now(ZoneOffset.UTC)).toMinutes();
        outboxService.publish(
                "ESCALATION_UNACKNOWLEDGED",
                new EscalationUnacknowledgedEvent(
                        escalation.getHospitalId(),
                        escalation.getId(),
                        escalation.getPatientId(),
                        escalation.getSeverity(),
                        escalation.getAssignedToId(),
                        minutesSinceCreation,
                        correlationId
                ),
                escalation.getHospitalId()
        );
    }
}
