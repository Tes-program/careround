package com.careround.hospital.entity;

import com.careround.hospital.enums.ShiftType;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "shift_schedule")
@Getter
@Setter
@NoArgsConstructor
public class ShiftSchedule extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "ward_id", length = 36)
    private String wardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 10)
    private ShiftType shiftType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "days_of_week", nullable = false, length = 50)
    private String daysOfWeek;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
