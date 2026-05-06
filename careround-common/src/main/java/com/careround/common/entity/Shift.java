package com.careround.common.entity;

import com.careround.common.enums.ShiftStatus;
import com.careround.common.enums.ShiftType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift",
    uniqueConstraints = @UniqueConstraint(name = "uq_shift_ward_type_start",
        columnNames = {"ward_id", "type", "start_time"}))
public class Shift {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ward_id", length = 36, nullable = false)
    private String wardId;

    /** Nullable — manually created shifts may not come from a schedule */
    @Column(name = "shift_schedule_id", length = 36)
    private String shiftScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ShiftType type;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** Assigned by WardSupervisor once PENDING_ASSIGNMENT */
    @Column(name = "lead_doctor_id", length = 36)
    private String leadDoctorId;

    @Column(name = "nurse_in_charge_id", length = 36)
    private String nurseInChargeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ShiftStatus status = ShiftStatus.PENDING_ASSIGNMENT;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getShiftScheduleId() { return shiftScheduleId; }
    public void setShiftScheduleId(String shiftScheduleId) { this.shiftScheduleId = shiftScheduleId; }
    public ShiftType getType() { return type; }
    public void setType(ShiftType type) { this.type = type; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getLeadDoctorId() { return leadDoctorId; }
    public void setLeadDoctorId(String leadDoctorId) { this.leadDoctorId = leadDoctorId; }
    public String getNurseInChargeId() { return nurseInChargeId; }
    public void setNurseInChargeId(String nurseInChargeId) { this.nurseInChargeId = nurseInChargeId; }
    public ShiftStatus getStatus() { return status; }
    public void setStatus(ShiftStatus status) { this.status = status; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
