package com.careround.patient.entity;

import com.careround.patient.enums.AssignedToRole;
import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskSource;
import com.careround.patient.enums.TaskStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_task")
@Getter
@Setter
@NoArgsConstructor
public class CareTask extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "round_id", length = 36)
    private String roundId;

    @Column(name = "created_by_id", nullable = false, length = 36)
    private String createdById;

    @Column(name = "assigned_to_id", length = 36)
    private String assignedToId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_to_role", length = 20)
    private AssignedToRole assignedToRole;

    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TaskSource source;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TaskPriority priority = TaskPriority.ROUTINE;

    @Column(name = "window_start")
    private LocalDateTime windowStart;

    @Column(name = "window_end")
    private LocalDateTime windowEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "completed_by_id", length = 36)
    private String completedById;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "workload_conflict", nullable = false)
    private boolean workloadConflict = false;

    @Column(name = "workload_conflict_reason", columnDefinition = "TEXT")
    private String workloadConflictReason;
}
