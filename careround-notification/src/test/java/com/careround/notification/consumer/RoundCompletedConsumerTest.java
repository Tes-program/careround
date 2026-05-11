package com.careround.notification.consumer;

import com.careround.notification.notification.Notification;
import com.careround.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundCompletedConsumerTest {

    @Mock private NotificationService notificationService;
    @Mock private NotificationIdempotencyGuard idempotencyGuard;

    private RoundCompletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new RoundCompletedConsumer(new ObjectMapper(), notificationService, idempotencyGuard);
    }

    @Test
    void validPayload_createsNotificationForLeadDoctor() {
        consumer.listen("""
                {"hospitalId":"hosp-1","roundId":"round-1","wardId":"ward-1","roundType":"MORNING","leadDoctorId":"doc-1","completedAt":"2026-05-11T10:00:00","correlationId":"corr-1"}
                """);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).persistAndSend(captor.capture());
        assertThat(captor.getValue().getRecipientId()).isEqualTo("doc-1");
        assertThat(captor.getValue().getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void duplicateCorrelationId_skipped() {
        when(idempotencyGuard.alreadyProcessed("corr-1")).thenReturn(true);

        consumer.listen("""
                {"hospitalId":"hosp-1","leadDoctorId":"doc-1","correlationId":"corr-1"}
                """);

        verify(notificationService, never()).persistAndSend(org.mockito.ArgumentMatchers.any());
    }
}
