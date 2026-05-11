package com.careround.notification.service;

import com.careround.notification.notification.Notification;
import com.careround.notification.notification.NotificationRepository;
import com.careround.notification.notification.NotificationStatus;
import com.careround.notification.provider.EmailNotificationProvider;
import com.careround.notification.provider.SmsNotificationProvider;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailNotificationProvider emailNotificationProvider;
    @Mock private SmsNotificationProvider smsNotificationProvider;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, emailNotificationProvider, smsNotificationProvider);
        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void persistAndSend_happyPath_emailChannel_savesAndSends() {
        Notification notification = notification("EMAIL");

        notificationService.persistAndSend(notification);

        verify(emailNotificationProvider).send(notification);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
    }

    @Test
    void persistAndSend_happyPath_smsChannel_savesAndSends() {
        Notification notification = notification("SMS");

        notificationService.persistAndSend(notification);

        verify(smsNotificationProvider).send(notification);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void persistAndSend_duplicateCorrelationId_skipsProcessing() {
        Notification notification = notification("EMAIL");
        when(notificationRepository.existsByCorrelationIdAndRecipientIdAndChannel("corr-1", "user-1", "EMAIL"))
                .thenReturn(true);

        notificationService.persistAndSend(notification);

        verify(notificationRepository, never()).save(any());
        verify(emailNotificationProvider, never()).send(any());
    }

    @Test
    void persistAndSend_providerFails_savesWithFailedStatus() {
        Notification notification = notification("EMAIL");
        doThrow(new RuntimeException("provider down")).when(emailNotificationProvider).send(notification);

        notificationService.persistAndSend(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getFailureReason()).isEqualTo("provider down");
    }

    @Test
    void persistAndSend_circuitOpen_savesWithFailedStatus() {
        Notification notification = notification("EMAIL");
        doThrow(CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults("emailProvider")))
                .when(emailNotificationProvider).send(notification);

        notificationService.persistAndSend(notification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(captor.getValue().getFailureReason()).isEqualTo("Circuit breaker open");
    }

    private Notification notification(String channel) {
        Notification notification = new Notification();
        notification.setHospitalId("hosp-1");
        notification.setRecipientId("user-1");
        notification.setRecipientType("USER");
        notification.setChannel(channel);
        notification.setSubject("Subject");
        notification.setBody("Body");
        notification.setEventType("event");
        notification.setCorrelationId("corr-1");
        return notification;
    }
}
