package com.careround.notification.dlt.repository;

import com.careround.notification.dlt.entity.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedNotificationRepository extends JpaRepository<FailedNotification, String> {
}
