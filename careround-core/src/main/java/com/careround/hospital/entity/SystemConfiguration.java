package com.careround.hospital.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_configurations")
public class SystemConfiguration extends BaseEntity {

    @Column(name = "news_amber_threshold", nullable = false)
    private int newsAmberThreshold = 5;

    @Column(name = "news_red_threshold", nullable = false)
    private int newsRedThreshold = 7;

    @Column(name = "task_overdue_grace_minutes", nullable = false)
    private int taskOverdueGraceMinutes = 30;

    @Column(name = "round_notifications_enabled", nullable = false)
    private boolean roundNotificationsEnabled = true;

    @Column(name = "nok_notification_enabled", nullable = false)
    private boolean nokNotificationEnabled = true;

    // Getters and Setters
    public int getNewsAmberThreshold() { return newsAmberThreshold; }
    public void setNewsAmberThreshold(int newsAmberThreshold) { this.newsAmberThreshold = newsAmberThreshold; }
    public int getNewsRedThreshold() { return newsRedThreshold; }
    public void setNewsRedThreshold(int newsRedThreshold) { this.newsRedThreshold = newsRedThreshold; }
    public int getTaskOverdueGraceMinutes() { return taskOverdueGraceMinutes; }
    public void setTaskOverdueGraceMinutes(int taskOverdueGraceMinutes) { this.taskOverdueGraceMinutes = taskOverdueGraceMinutes; }
    public boolean isRoundNotificationsEnabled() { return roundNotificationsEnabled; }
    public void setRoundNotificationsEnabled(boolean roundNotificationsEnabled) { this.roundNotificationsEnabled = roundNotificationsEnabled; }
    public boolean isNokNotificationEnabled() { return nokNotificationEnabled; }
    public void setNokNotificationEnabled(boolean nokNotificationEnabled) { this.nokNotificationEnabled = nokNotificationEnabled; }
}
