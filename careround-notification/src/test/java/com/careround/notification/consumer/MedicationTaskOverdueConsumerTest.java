package com.careround.notification.consumer;

import com.careround.notification.event.MedicationTaskOverdueEvent;
import com.careround.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MedicationTaskOverdueConsumerTest {

    @Mock private NotificationService notificationService;
    @Spy  private ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @InjectMocks private MedicationTaskOverdueConsumer consumer;

    private static final String NURSE_ID   = "nurse-1";
    private static final String HOSPITAL_ID = "hosp-1";
    private static final String TOKEN      = "fcm-token-abc";

    @Test
    void consume_delegatesToNotificationService_withEventFields() throws Exception {
        consumer.consume(record(event(TOKEN)));

        verify(notificationService).sendTaskOverdue(
                eq(TOKEN),
                eq("task-1"),
                eq("patient-1"),
                eq("Alice Smith"),
                eq("Aspirin"),
                eq("100mg"),
                eq(45)
        );
    }

    @Test
    void consume_passesNullToken_whenDeviceTokenAbsent() throws Exception {
        consumer.consume(record(event(null)));

        // NotificationService receives null token — it is responsible for the skip logic
        verify(notificationService).sendTaskOverdue(
                eq(null), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void consume_propagatesException_whenNotificationServiceThrows() throws Exception {
        doThrow(new RuntimeException("FCM send failed"))
                .when(notificationService).sendTaskOverdue(any(), any(), any(), any(), any(), any(), anyInt());

        assertThatThrownBy(() -> consumer.consume(record(event(TOKEN))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FCM send failed");
    }

    @Test
    void consume_throwsRuntimeException_onDeserializationFailure() {
        ConsumerRecord<String, String> badRecord =
                new ConsumerRecord<>("medication-task-overdue", 0, 0L, "key", "not-valid-json{{");

        assertThatThrownBy(() -> consumer.consume(badRecord))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to deserialize");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private MedicationTaskOverdueEvent event(String deviceToken) {
        return new MedicationTaskOverdueEvent(
                "evt-1", "task-1", "patient-1", "ward-1", HOSPITAL_ID, NURSE_ID,
                "Aspirin", "100mg", LocalDateTime.now().minusMinutes(45), 45L,
                "corr-1", LocalDateTime.now(),
                "Alice Smith", deviceToken);
    }

    private ConsumerRecord<String, String> record(MedicationTaskOverdueEvent event) throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        String json = mapper.writeValueAsString(event);
        return new ConsumerRecord<>("medication-task-overdue", 0, 0L, HOSPITAL_ID, json);
    }
}
