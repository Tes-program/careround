package com.careround.patient.entity;

import com.careround.patient.enums.ConsciousnessLevel;
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

    @Column(name = "recorded_by_id", nullable = false, length = 36)
    private String recordedById;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "oxygen_saturation", precision = 5, scale = 2)
    private BigDecimal oxygenSaturation;

    @Column(name = "systolic_bp")
    private Integer systolicBP;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperature;

    @Enumerated(EnumType.STRING)
    @Column(name = "consciousness_level", length = 20)
    private ConsciousnessLevel consciousnessLevel;

    @Column(name = "news_score", nullable = false)
    private int newsScore;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
