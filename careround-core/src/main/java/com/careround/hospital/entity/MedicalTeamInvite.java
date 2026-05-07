package com.careround.hospital.entity;

import com.careround.hospital.enums.InviteStatus;
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
@Table(name = "medical_team_invite")
@Getter
@Setter
@NoArgsConstructor
public class MedicalTeamInvite extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "medical_team_id", nullable = false, length = 36)
    private String medicalTeamId;

    @Column(name = "invited_user_id", nullable = false, length = 36)
    private String invitedUserId;

    @Column(name = "invited_by_id", nullable = false, length = 36)
    private String invitedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
