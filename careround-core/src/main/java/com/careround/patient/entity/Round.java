package com.careround.patient.entity;

import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;
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
@Table(name = "round")
@Getter
@Setter
@NoArgsConstructor
public class Round extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "medical_team_id", nullable = false, length = 36)
    private String medicalTeamId;

    @Column(name = "shift_id", nullable = false, length = 36)
    private String shiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 20)
    private RoundType roundType;

    @Column(name = "lead_doctor_id", nullable = false, length = 36)
    private String leadDoctorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoundStatus status = RoundStatus.SCHEDULED;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "team_members", columnDefinition = "TEXT")
    private String teamMembers;
}
