package com.careround.patient.medicationtask.entity;

import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_task",
        indexes = {
                @Index(name = "idx_task_hospital_status_time",
                        columnList = "hospital_id, status, scheduled_time"),
                @Index(name = "idx_task_ward_status",
                        columnList = "ward_id, status, scheduled_time"),
                @Index(name = "idx_task_nurse_status",
                        columnList = "assigned_nurse_id, status, scheduled_time")
        })
@Getter
@Setter
@NoArgsConstructor
public class MedicationTask extends BaseEntity {

    @Column(name = "medication_chart_id", nullable = false, length = 36)
    private String medicationChartId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "assigned_nurse_id", length = 36)
    private String assignedNurseId;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MedicationTaskStatus status = MedicationTaskStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by_id", length = 36)
    private String completedById;

    @Column(name = "actual_dose_given", length = 50)
    private String actualDoseGiven;

    @Column(name = "pre_reminder_sent_at")
    private LocalDateTime preReminderSentAt;

    @Column(name = "overdue_alert_sent_at")
    private LocalDateTime overdueAlertSentAt;
}
