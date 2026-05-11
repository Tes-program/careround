package com.careround.scheduler.jobs;

import com.careround.patient.entity.Escalation;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.repository.EscalationRepository;
import com.careround.scheduler.service.EscalationUnacknowledgedProcessor;
import com.careround.shared.event.EscalationUnacknowledgedEvent;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscalationUnacknowledgedJobTest {

    @Mock
    private EscalationRepository escalationRepository;

    @Mock
    private OutboxService outboxService;

    private EscalationUnacknowledgedProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new EscalationUnacknowledgedProcessor(escalationRepository, outboxService);
    }

    @Test
    void openEscalationOver15Min_publishesEvent() {
        Escalation escalation = openEscalation(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(20));
        when(escalationRepository.findAllByStatusAndCreatedAtBefore(eq(EscalationStatus.OPEN), any(LocalDateTime.class)))
                .thenReturn(List.of(escalation));

        int processed = processor.process("corr-1");

        assertThat(processed).isEqualTo(1);
        verify(outboxService).publish(eq("ESCALATION_UNACKNOWLEDGED"), any(EscalationUnacknowledgedEvent.class), eq("hosp-1"));
    }

    @Test
    void recentEscalation_skipped() {
        Escalation escalation = openEscalation(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        when(escalationRepository.findAllByStatusAndCreatedAtBefore(eq(EscalationStatus.OPEN), any(LocalDateTime.class)))
                .thenReturn(List.of(escalation));

        int processed = processor.process("corr-1");

        assertThat(processed).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void doesNotChangeEscalationStatus() {
        Escalation escalation = openEscalation(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(20));
        when(escalationRepository.findAllByStatusAndCreatedAtBefore(eq(EscalationStatus.OPEN), any(LocalDateTime.class)))
                .thenReturn(List.of(escalation));

        processor.process("corr-1");

        assertThat(escalation.getStatus()).isEqualTo(EscalationStatus.OPEN);
    }

    private Escalation openEscalation(LocalDateTime createdAt) {
        Escalation escalation = new Escalation();
        escalation.setId("esc-1");
        escalation.setHospitalId("hosp-1");
        escalation.setPatientId("patient-1");
        escalation.setSeverity(EscalationSeverity.RED);
        escalation.setAssignedToId("doctor-1");
        escalation.setStatus(EscalationStatus.OPEN);
        escalation.setCreatedAt(createdAt);
        return escalation;
    }
}
