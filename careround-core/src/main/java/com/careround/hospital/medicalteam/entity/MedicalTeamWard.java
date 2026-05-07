package com.careround.hospital.medicalteam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_team_wards")
public class MedicalTeamWard {

    @EmbeddedId
    private MedicalTeamWardId id;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public MedicalTeamWard() {}

    public MedicalTeamWard(MedicalTeamWardId id) {
        this.id = id;
    }

    public MedicalTeamWardId getId() { return id; }
    public void setId(MedicalTeamWardId id) { this.id = id; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
