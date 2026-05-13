package com.careround.notification.service;

import com.careround.notification.dto.NotificationResponse;
import com.careround.notification.dto.UnreadCountResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> listNotifications();

    UnreadCountResponse unreadCount();

    void markRead(String notificationId);

    void markAllRead();
}
