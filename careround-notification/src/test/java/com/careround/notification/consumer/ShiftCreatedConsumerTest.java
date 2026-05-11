package com.careround.notification.consumer;

import com.careround.notification.client.CoreLookupClient;
import com.careround.notification.notification.Notification;
import com.careround.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftCreatedConsumerTest {

    @Mock private NotificationService notificationService;
    @Mock private CoreLookupClient coreLookupClient;
    @Mock private NotificationIdempotencyGuard idempotencyGuard;

    private ShiftCreatedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ShiftCreatedConsumer(new ObjectMapper(), notificationService, coreLookupClient, idempotencyGuard);
    }

    @Test
    void supervisorExists_notified() {
        when(coreLookupClient.findWardSupervisorId("ward-1")).thenReturn(Optional.of("super-1"));

        consumer.listen(payload());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).persistAndSend(captor.capture());
        assertThat(captor.getValue().getRecipientId()).isEqualTo("super-1");
        assertThat(captor.getValue().getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void supervisorNull_skipped() {
        when(coreLookupClient.findWardSupervisorId("ward-1")).thenReturn(Optional.empty());

        consumer.listen(payload());

        verify(notificationService, never()).persistAndSend(org.mockito.ArgumentMatchers.any());
    }

    private String payload() {
        return """
                {"hospitalId":"hosp-1","shiftId":"shift-1","wardId":"ward-1","shiftType":"DAY","startTime":"2026-05-11T08:00:00","endTime":"2026-05-11T20:00:00","correlationId":"corr-1"}
                """;
    }
}
