package com.careround.common.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "system_configuration")
public class SystemConfiguration {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, unique = true, nullable = false)
    private String hospitalId;

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

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
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
