package com.careround.notification.dlt;

import com.careround.notification.dlt.entity.FailedNotification;
import com.careround.notification.dlt.repository.FailedNotificationRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationDltConsumerTest {

    @Mock private FailedNotificationRepository failedNotificationRepository;

    private NotificationDltConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new NotificationDltConsumer(failedNotificationRepository, new ObjectMapper());
    }

    @Test
    void dltMessage_persistsFailedNotification() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "careround.task.overdue.DLT", 1, 42L, "key",
                "{\"hospitalId\":\"hosp-1\",\"correlationId\":\"corr-1\"}");

        consumer.handleDlt(record);

        ArgumentCaptor<FailedNotification> captor = ArgumentCaptor.forClass(FailedNotification.class);
        verify(failedNotificationRepository).save(captor.capture());
        assertThat(captor.getValue().getTopic()).isEqualTo("careround.task.overdue.DLT");
        assertThat(captor.getValue().getHospitalId()).isEqualTo("hosp-1");
        assertThat(captor.getValue().getCorrelationId()).isEqualTo("corr-1");
    }
}
