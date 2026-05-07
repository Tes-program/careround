package com.careround.notification.dlt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotification, String> {
}
