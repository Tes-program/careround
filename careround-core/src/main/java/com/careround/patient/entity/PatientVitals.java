package com.careround.patient.entity;

import com.careround.patient.enums.VhiStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_vitals")
@Getter
@Setter
@NoArgsConstructor
public class PatientVitals extends BaseEntity {

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "recorded_by_id", nullable = false, length = 36)
    private String recordedById;

    @Column(name = "pulse")
    private Integer pulse;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(name = "spo2", precision = 5, scale = 2)
    private BigDecimal spo2;

    @Column(name = "vhi_score", nullable = false)
    private int vhiScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "vhi_status", nullable = false, length = 10)
    private VhiStatus vhiStatus;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
