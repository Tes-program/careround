package com.careround.hospital.entity;

import com.careround.hospital.enums.OnCallRole;
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
@Table(name = "on_call_rotation")
@Getter
@Setter
@NoArgsConstructor
public class OnCallRotation extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "department_id", nullable = false, length = 36)
    private String departmentId;

    @Column(name = "ward_id", length = 36)
    private String wardId;

    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OnCallRole role;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
}
