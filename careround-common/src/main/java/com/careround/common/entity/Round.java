package com.careround.common.entity;

import com.careround.common.enums.RoundStatus;
import com.careround.common.enums.RoundType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "round")
public class Round {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ward_id", length = 36, nullable = false)
    private String wardId;

    @Column(name = "medical_team_id", length = 36, nullable = false)
    private String medicalTeamId;

    @Column(name = "shift_id", length = 36, nullable = false)
    private String shiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 20)
    private RoundType roundType;

    @Column(name = "lead_doctor_id", length = 36, nullable = false)
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

    /** Comma-separated User UUIDs of all participants in this round */
    @Column(name = "team_members", columnDefinition = "TEXT")
    private String teamMembers;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getMedicalTeamId() { return medicalTeamId; }
    public void setMedicalTeamId(String medicalTeamId) { this.medicalTeamId = medicalTeamId; }
    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }
    public RoundType getRoundType() { return roundType; }
    public void setRoundType(RoundType roundType) { this.roundType = roundType; }
    public String getLeadDoctorId() { return leadDoctorId; }
    public void setLeadDoctorId(String leadDoctorId) { this.leadDoctorId = leadDoctorId; }
    public RoundStatus getStatus() { return status; }
    public void setStatus(RoundStatus status) { this.status = status; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getTeamMembers() { return teamMembers; }
    public void setTeamMembers(String teamMembers) { this.teamMembers = teamMembers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
