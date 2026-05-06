package com.careround.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Join table: members of a MedicalTeam. Composite PK. */
@Entity
@Table(name = "medical_team_member")
public class MedicalTeamMember {

    @EmbeddedId
    private MedicalTeamMemberId id;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    void prePersist() {
        if (this.joinedAt == null) this.joinedAt = LocalDateTime.now();
    }

    public MedicalTeamMemberId getId() { return id; }
    public void setId(MedicalTeamMemberId id) { this.id = id; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
