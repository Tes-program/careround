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

    @Column(name = "task_overdue_reminder_minutes", nullable = false)
    private int taskOverdueReminderMinutes = 10;

    @Column(name = "task_escalation_minutes", nullable = false)
    private int taskEscalationMinutes = 20;

    @Column(name = "push_notifications_enabled", nullable = false)
    private boolean pushNotificationsEnabled = true;
}
