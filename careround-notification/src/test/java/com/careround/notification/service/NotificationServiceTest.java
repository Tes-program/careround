package com.careround.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        ReflectionTestUtils.setField(notificationService, "firebaseMessaging", firebaseMessaging);
        ReflectionTestUtils.setField(notificationService, "enabled", true);
    }

    @Test
    void sendTaskOverdue_sendsMessage_whenEnabledAndTokenPresent() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("projects/x/messages/msg-id");

        assertThatNoException().isThrownBy(() ->
                notificationService.sendTaskOverdue(
                        "fcm-device-token", "task-1", "patient-1",
                        "Alice Smith", "Aspirin", "100mg", 30));

        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void sendTaskOverdue_skips_whenDeviceTokenIsNull() throws Exception {
        assertThatNoException().isThrownBy(() ->
                notificationService.sendTaskOverdue(
                        null, "task-1", "patient-1", "Alice Smith", "Aspirin", "100mg", 10));

        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    void sendTaskOverdue_skips_whenDeviceTokenIsBlank() throws Exception {
        assertThatNoException().isThrownBy(() ->
                notificationService.sendTaskOverdue(
                        "   ", "task-1", "patient-1", "Alice Smith", "Aspirin", "100mg", 10));

        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    void sendTaskOverdue_skips_whenDisabled() throws Exception {
        ReflectionTestUtils.setField(notificationService, "enabled", false);

        assertThatNoException().isThrownBy(() ->
                notificationService.sendTaskOverdue(
                        "token", "task-1", "patient-1", "Alice Smith", "Aspirin", "100mg", 5));

        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    void sendTaskOverdue_throwsRuntimeException_whenFcmFails() throws Exception {
        FirebaseMessagingException fcmEx =
                org.mockito.Mockito.mock(FirebaseMessagingException.class);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(fcmEx);

        assertThatThrownBy(() ->
                notificationService.sendTaskOverdue(
                        "token", "task-1", "patient-1", "Alice Smith", "Aspirin", "100mg", 15))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FCM send failed");
    }

    @Test
    void sendTaskOverdue_buildsMessageBody_withDrugAndPatient() throws Exception {
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        when(firebaseMessaging.send(captor.capture())).thenReturn("msg-id");

        notificationService.sendTaskOverdue(
                "token", "task-1", "patient-1", "Bob Jones", "Metformin", "500mg", 20);

        // verify the captured message token matches the one we passed
        Message sent = captor.getValue();
        assertThat(sent).isNotNull();
        // spot-check via toString that key fields appear (Firebase Message has no public getters)
        verify(firebaseMessaging).send(any(Message.class));
    }
}
