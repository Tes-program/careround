package com.careround.hospital.medicalteam.entity;

import com.careround.shared.entity.BaseEntity;
import com.careround.shared.enums.InviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_team_invites")
public class MedicalTeamInvite extends BaseEntity {

    @Column(name = "medical_team_id", nullable = false, length = 36)
    private String medicalTeamId;

    @Column(name = "invited_user_id", nullable = false, length = 36)
    private String invitedUserId;

    @Column(name = "invited_by_id", nullable = false, length = 36)
    private String invitedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Getters and Setters
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
}
