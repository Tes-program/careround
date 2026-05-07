package com.careround.hospital.shift.entity;

import com.careround.shared.enums.ShiftStatus;
import com.careround.shared.enums.ShiftType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shifts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ward_id", "type", "start_time"})
})
public class Shift {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "shift_schedule_id", length = 36)
    private String shiftScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType type;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "lead_doctor_id", length = 36)
    private String leadDoctorId;

    @Column(name = "nurse_in_charge_id", length = 36)
    private String nurseInChargeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status = ShiftStatus.PENDING_ASSIGNMENT;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
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
