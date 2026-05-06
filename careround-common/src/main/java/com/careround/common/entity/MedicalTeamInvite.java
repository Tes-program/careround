package com.careround.common.entity;

import com.careround.common.enums.InviteStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_team_invite")
public class MedicalTeamInvite {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    @Column(name = "medical_team_id", length = 36, nullable = false)
    private String medicalTeamId;

    @Column(name = "invited_user_id", length = 36, nullable = false)
    private String invitedUserId;

    @Column(name = "invited_by_id", length = 36, nullable = false)
    private String invitedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

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
    public String getMedicalTeamId() { return medicalTeamId; }
    public void setMedicalTeamId(String medicalTeamId) { this.medicalTeamId = medicalTeamId; }
    public String getInvitedUserId() { return invitedUserId; }
    public void setInvitedUserId(String invitedUserId) { this.invitedUserId = invitedUserId; }
    public String getInvitedById() { return invitedById; }
    public void setInvitedById(String invitedById) { this.invitedById = invitedById; }
    public InviteStatus getStatus() { return status; }
    public void setStatus(InviteStatus status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
