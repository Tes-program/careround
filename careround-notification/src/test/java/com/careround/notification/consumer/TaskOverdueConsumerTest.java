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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskOverdueConsumerTest {

    @Mock private NotificationService notificationService;
    @Mock private CoreLookupClient coreLookupClient;
    @Mock private NotificationIdempotencyGuard idempotencyGuard;

    private TaskOverdueConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TaskOverdueConsumer(new ObjectMapper(), notificationService, coreLookupClient, idempotencyGuard);
    }

    @Test
    void validPayload_notifiesAssigneeAndSupervisor() {
        when(coreLookupClient.findWardSupervisorId("ward-1")).thenReturn(Optional.of("super-1"));

        consumer.listen(payload("user-1"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService, org.mockito.Mockito.times(2)).persistAndSend(captor.capture());
        assertThat(captor.getAllValues()).extracting(Notification::getRecipientId)
                .containsExactly("user-1", "super-1");
    }

    @Test
    void assignedToIdNull_onlySupervisorNotified() {
        when(coreLookupClient.findWardSupervisorId("ward-1")).thenReturn(Optional.of("super-1"));

        consumer.listen(payload(null));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).persistAndSend(captor.capture());
        assertThat(captor.getValue().getRecipientId()).isEqualTo("super-1");
    }

    private String payload(String assignedToId) {
        return """
                {"hospitalId":"hosp-1","taskId":"task-1","patientId":"patient-1","wardId":"ward-1","assignedToId":%s,"title":"Vitals","windowEnd":"2026-05-11T10:00:00","correlationId":"corr-1"}
                """.formatted(assignedToId == null ? "null" : "\"" + assignedToId + "\"");
    }
}
