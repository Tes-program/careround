package com.careround.common.entity;

import com.careround.common.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "care_task")
public class CareTask {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "patient_id", length = 36, nullable = false)
    private String patientId;

    @Column(name = "ward_id", length = 36, nullable = false)
    private String wardId;

    /** Nullable for NURSING_CARE_PLAN tasks */
    @Column(name = "round_id", length = 36)
    private String roundId;

    @Column(name = "created_by_id", length = 36, nullable = false)
    private String createdById;

    @Column(name = "assigned_to_id", length = 36)
    private String assignedToId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_to_role", length = 20)
    private AssignedToRole assignedToRole;

    /** Free-text task category e.g. "Blood test", "IV antibiotics" */
    @Column(name = "task_type", length = 100)
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getRoundId() { return roundId; }
    public void setRoundId(String roundId) { this.roundId = roundId; }
    public String getCreatedById() { return createdById; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }
    public String getAssignedToId() { return assignedToId; }
    public void setAssignedToId(String assignedToId) { this.assignedToId = assignedToId; }
    public AssignedToRole getAssignedToRole() { return assignedToRole; }
    public void setAssignedToRole(AssignedToRole assignedToRole) { this.assignedToRole = assignedToRole; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public TaskSource getSource() { return source; }
    public void setSource(TaskSource source) { this.source = source; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }
    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public String getCompletedById() { return completedById; }
    public void setCompletedById(String completedById) { this.completedById = completedById; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
