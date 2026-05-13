package com.careround.notification.repository;

import com.careround.notification.entity.NotificationReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationReadReceiptRepository extends JpaRepository<NotificationReadReceipt, String> {

    List<NotificationReadReceipt> findAllByHospitalIdAndUserId(String hospitalId, String userId);

    Optional<NotificationReadReceipt> findByHospitalIdAndUserIdAndNotificationId(
            String hospitalId, String userId, String notificationId);
}
