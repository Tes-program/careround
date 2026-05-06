package com.careround.common.entity;

import com.careround.common.enums.ConsciousnessLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_vitals")
public class PatientVitals {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "patient_id", length = 36, nullable = false)
    private String patientId;

    @Column(name = "recorded_by_id", length = 36, nullable = false)
    private String recordedById;

    @Column(name = "heart_rate", nullable = false)
    private int heartRate;

    @Column(name = "respiratory_rate", nullable = false)
    private int respiratoryRate;

    /** DECIMAL(5,2) per schema — e.g. 98.50 */
    @Column(name = "oxygen_saturation", nullable = false, precision = 5, scale = 2)
    private BigDecimal oxygenSaturation;

    @Column(name = "systolic_bp", nullable = false)
    private int systolicBP;

    /** DECIMAL(4,1) per schema — e.g. 37.5 */
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal temperature;

    @Enumerated(EnumType.STRING)
    @Column(name = "consciousness_level", nullable = false, length = 20)
    private ConsciousnessLevel consciousnessLevel;

    /** Computed by NewsScoreService on save */
    @Column(name = "news_score", nullable = false)
    private int newsScore;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getRecordedById() { return recordedById; }
    public void setRecordedById(String recordedById) { this.recordedById = recordedById; }
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }
    public int getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(int respiratoryRate) { this.respiratoryRate = respiratoryRate; }
    public BigDecimal getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(BigDecimal oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }
    public int getSystolicBP() { return systolicBP; }
    public void setSystolicBP(int systolicBP) { this.systolicBP = systolicBP; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public ConsciousnessLevel getConsciousnessLevel() { return consciousnessLevel; }
    public void setConsciousnessLevel(ConsciousnessLevel consciousnessLevel) { this.consciousnessLevel = consciousnessLevel; }
    public int getNewsScore() { return newsScore; }
    public void setNewsScore(int newsScore) { this.newsScore = newsScore; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
