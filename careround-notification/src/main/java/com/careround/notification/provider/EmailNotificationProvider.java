package com.careround.notification.provider;

import com.careround.notification.notification.Notification;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailNotificationProvider {

    private final CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("emailProvider");

    public void send(Notification notification) {
        circuitBreaker.executeRunnable(() -> {
            log.info("action=EMAIL_SEND recipient={} subject={}",
                    notification.getRecipientId(), notification.getSubject());
        });
    }
}
