package com.careround.notification.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_read_receipt", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_notification_read_user",
                columnNames = {"hospital_id", "user_id", "notification_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class NotificationReadReceipt extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "notification_id", nullable = false, length = 120)
    private String notificationId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
