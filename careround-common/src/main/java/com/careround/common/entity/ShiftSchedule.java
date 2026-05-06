package com.careround.common.entity;

import com.careround.common.enums.ShiftType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Defines a recurring shift pattern. ShiftAutoCreatorJob reads active rows
 * and materialises Shift records at the configured startTime on matching days.
 */
@Entity
@Table(name = "shift_schedule")
public class ShiftSchedule {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    /** Nullable = applies to ALL wards in the hospital */
    @Column(name = "ward_id", length = 36)
    private String wardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 10)
    private ShiftType shiftType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Comma-separated day abbreviations: MON,TUE,WED,THU,FRI,SAT,SUN */
    @Column(name = "days_of_week", nullable = false, length = 50)
    private String daysOfWeek;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
