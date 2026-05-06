package com.careround.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Join table: which wards a MedicalTeam operates on. Composite PK. */
@Entity
@Table(name = "medical_team_ward")
public class MedicalTeamWard {

    @EmbeddedId
    private MedicalTeamWardId id;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    void prePersist() {
        if (this.assignedAt == null) this.assignedAt = LocalDateTime.now();
    }

    public MedicalTeamWardId getId() { return id; }
    public void setId(MedicalTeamWardId id) { this.id = id; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
