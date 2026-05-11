package com.careround.notification.provider;

import com.careround.notification.notification.Notification;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsNotificationProvider {

    private final CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("smsProvider");

    public void send(Notification notification) {
        circuitBreaker.executeRunnable(() -> {
            log.info("action=SMS_SEND recipient={} hospitalId={}",
                    notification.getRecipientId(), notification.getHospitalId());
        });
    }
}
