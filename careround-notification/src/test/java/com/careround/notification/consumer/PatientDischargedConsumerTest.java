package com.careround.notification.consumer;

import com.careround.notification.client.CoreLookupClient;
import com.careround.notification.client.NextOfKinContact;
import com.careround.notification.notification.Notification;
import com.careround.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientDischargedConsumerTest {

    @Mock private NotificationService notificationService;
    @Mock private CoreLookupClient coreLookupClient;
    @Mock private NotificationIdempotencyGuard idempotencyGuard;

    private PatientDischargedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PatientDischargedConsumer(new ObjectMapper(), notificationService, coreLookupClient, idempotencyGuard);
    }

    @Test
    void nokWithConsent_notified() {
        when(coreLookupClient.findNextOfKin("patient-1"))
                .thenReturn(List.of(new NextOfKinContact("nok-1", "EMAIL", true)));

        consumer.listen(payload());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).persistAndSend(captor.capture());
        assertThat(captor.getValue().getRecipientId()).isEqualTo("nok-1");
    }

    @Test
    void nokWithoutConsent_skipped() {
        when(coreLookupClient.findNextOfKin("patient-1"))
                .thenReturn(List.of(new NextOfKinContact("nok-1", "EMAIL", false)));

        consumer.listen(payload());

        verify(notificationService, never()).persistAndSend(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void respectsPreferredContactMethod() {
        when(coreLookupClient.findNextOfKin("patient-1"))
                .thenReturn(List.of(new NextOfKinContact("nok-1", "SMS", true)));

        consumer.listen(payload());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).persistAndSend(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("SMS");
    }

    private String payload() {
        return """
                {"hospitalId":"hosp-1","patientId":"patient-1","wardId":"ward-1","dischargedAt":"2026-05-11T10:00:00","correlationId":"corr-1"}
                """;
    }
}
