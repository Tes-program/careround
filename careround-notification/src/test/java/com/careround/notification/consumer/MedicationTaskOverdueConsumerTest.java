package com.careround.notification.consumer;

import com.careround.notification.event.MedicationTaskOverdueEvent;
import com.careround.notification.repository.NurseTokenRepository;
import com.careround.notification.service.FcmService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationTaskOverdueConsumerTest {

    @Mock private NurseTokenRepository nurseTokenRepository;
    @Mock private FcmService fcmService;
    @Spy private ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @InjectMocks private MedicationTaskOverdueConsumer consumer;

    private static final String NURSE_ID = "nurse-1";
    private static final String HOSPITAL_ID = "hosp-1";
    private static final String TOKEN = "fcm-token-abc";

    @Test
    void consume_sendsFcmNotification_whenTokenPresent() throws Exception {
        when(nurseTokenRepository.findFcmToken(NURSE_ID, HOSPITAL_ID)).thenReturn(Optional.of(TOKEN));

        consumer.consume(record(event()));

        verify(fcmService).send(eq(TOKEN), anyString(), anyString());
    }

    @Test
    void consume_skipsNotification_whenTokenAbsent() throws Exception {
        when(nurseTokenRepository.findFcmToken(NURSE_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        consumer.consume(record(event()));

        verify(fcmService, never()).send(any(), any(), any());
    }

    @Test
    void consume_includesDrugName_andDose_andMinutesOverdue_inBody() throws Exception {
        when(nurseTokenRepository.findFcmToken(NURSE_ID, HOSPITAL_ID)).thenReturn(Optional.of(TOKEN));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        consumer.consume(record(event()));

        verify(fcmService).send(any(), any(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue())
                .contains("Aspirin")
                .contains("100mg")
                .contains("45");
    }

    @Test
    void consume_usesHospitalIdFromEvent_notContextHolder() throws Exception {
        when(nurseTokenRepository.findFcmToken(NURSE_ID, HOSPITAL_ID)).thenReturn(Optional.of(TOKEN));

        consumer.consume(record(event()));

        verify(nurseTokenRepository).findFcmToken(NURSE_ID, HOSPITAL_ID);
    }

    @Test
    void consume_doesNotThrow_whenFcmSendFails() throws Exception {
        when(nurseTokenRepository.findFcmToken(NURSE_ID, HOSPITAL_ID)).thenReturn(Optional.of(TOKEN));
        doThrow(new RuntimeException("FCM unavailable")).when(fcmService).send(any(), any(), any());

        consumer.consume(record(event()));
        // no exception propagated — error handler at container level would handle retries
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private MedicationTaskOverdueEvent event() {
        return new MedicationTaskOverdueEvent(
                "evt-1", "task-1", "patient-1", "ward-1", HOSPITAL_ID, NURSE_ID,
                "Aspirin", "100mg", LocalDateTime.now().minusMinutes(45), 45L,
                "corr-1", LocalDateTime.now());
    }

    private ConsumerRecord<String, String> record(MedicationTaskOverdueEvent event) throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        String json = mapper.writeValueAsString(event);
        return new ConsumerRecord<>("medication-task-overdue", 0, 0L, HOSPITAL_ID, json);
    }
}
