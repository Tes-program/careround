package com.careround.hospital.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_configuration")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfiguration extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, unique = true, length = 36)
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
}
