package com.careround.common.entity;

import com.careround.common.enums.OnCallRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "on_call_rotation")
public class OnCallRotation {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    @Column(name = "department_id", length = 36, nullable = false)
    private String departmentId;

    /** Nullable — if set, overrides department-level routing for a specific ward */
    @Column(name = "ward_id", length = 36)
    private String wardId;

    @Column(name = "doctor_id", length = 36, nullable = false)
    private String doctorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OnCallRole role;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
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
