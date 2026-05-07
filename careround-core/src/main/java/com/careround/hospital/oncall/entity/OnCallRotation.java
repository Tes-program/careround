package com.careround.hospital.oncall.entity;

import com.careround.shared.entity.BaseEntity;
import com.careround.shared.enums.OnCallRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "on_call_rotations")
public class OnCallRotation extends BaseEntity {

    @Column(name = "department_id", nullable = false, length = 36)
    private String departmentId;

    @Column(name = "ward_id", length = 36)
    private String wardId;

    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnCallRole role;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Getters and Setters
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public OnCallRole getRole() { return role; }
    public void setRole(OnCallRole role) { this.role = role; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
