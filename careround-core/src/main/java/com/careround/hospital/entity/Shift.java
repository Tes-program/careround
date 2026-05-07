package com.careround.hospital.entity;

import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.enums.ShiftType;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ward_id", "type", "start_time"})
})
@Getter
@Setter
@NoArgsConstructor
public class Shift extends BaseEntity {

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "shift_schedule_id", length = 36)
    private String shiftScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
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
    @Column(nullable = false, length = 30)
    private ShiftStatus status = ShiftStatus.PENDING_ASSIGNMENT;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
