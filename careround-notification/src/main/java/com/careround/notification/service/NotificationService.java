package com.careround.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    /** Null when firebase.enabled=false — all send calls short-circuit before use. */
    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    @Value("${firebase.enabled:true}")
    private boolean enabled;

    public void sendTaskOverdue(
            String deviceToken,
            String taskId,
            String patientId,
            String patientName,
            String drugName,
            String dose,
            int minutesOverdue) {

        if (!enabled) {
            log.info("[DEV] FCM disabled — skipping notification for task {}", taskId);
            return;
        }

        if (deviceToken == null || deviceToken.isBlank()) {
            log.warn("No FCM token for task {} — notification skipped", taskId);
            return;
        }

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                        .setTitle("Medication Overdue")
                        .setBody(drugName + " " + dose + " for "
                                + patientName + " — " + minutesOverdue + " min overdue")
                        .build())
                .putData("type", "MEDICATION_TASK_OVERDUE")
                .putData("taskId", taskId)
                .putData("patientId", patientId)
                .putData("patientName", patientName != null ? patientName : "")
                .putData("drugName", drugName)
                .putData("dose", dose)
                .putData("minutesOverdue", String.valueOf(minutesOverdue))
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("FCM sent — messageId: {}, taskId: {}", messageId, taskId);
        } catch (FirebaseMessagingException e) {
            log.error("FCM failed for task {}: {}", taskId, e.getMessage());
            throw new RuntimeException("FCM send failed", e);
        }
    }
}
