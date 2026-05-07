package com.careround.hospital.medicalteam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_team_members")
public class MedicalTeamMember {

    @EmbeddedId
    private MedicalTeamMemberId id;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public MedicalTeamMember() {}

    public MedicalTeamMember(MedicalTeamMemberId id) {
        this.id = id;
    }

    public MedicalTeamMemberId getId() { return id; }
    public void setId(MedicalTeamMemberId id) { this.id = id; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
