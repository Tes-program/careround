package com.careround.common.entity;

import com.careround.common.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "escalation")
public class Escalation {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    @Column(name = "patient_id", length = 36, nullable = false)
    private String patientId;

    /** Nullable — system-generated escalations have no human trigger */
    @Column(name = "triggered_by_id", length = 36)
    private String triggeredById;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 25)
    private EscalationTrigger triggerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EscalationSeverity severity;

    @Column(name = "assigned_to_id", length = 36)
    private String assignedToId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscalationStatus status = EscalationStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getTriggeredById() { return triggeredById; }
    public void setTriggeredById(String triggeredById) { this.triggeredById = triggeredById; }
    public EscalationTrigger getTriggerType() { return triggerType; }
    public void setTriggerType(EscalationTrigger triggerType) { this.triggerType = triggerType; }
    public EscalationSeverity getSeverity() { return severity; }
    public void setSeverity(EscalationSeverity severity) { this.severity = severity; }
    public String getAssignedToId() { return assignedToId; }
    public void setAssignedToId(String assignedToId) { this.assignedToId = assignedToId; }
    public EscalationStatus getStatus() { return status; }
    public void setStatus(EscalationStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
