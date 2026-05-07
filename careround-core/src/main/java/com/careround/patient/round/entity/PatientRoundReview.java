package com.careround.patient.round.entity;

import com.careround.shared.enums.ClinicalStatus;
import com.careround.shared.enums.DischargeAssessment;
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
@Table(name = "patient_round_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"round_id", "patient_id"})
})
public class PatientRoundReview {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "round_id", nullable = false, length = 36)
    private String roundId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "reviewed_by_id", nullable = false, length = 36)
    private String reviewedById;

    @Column(name = "review_order", nullable = false)
    private int reviewOrder;

    @Column(name = "news_score_at_review", nullable = false)
    private int newsScoreAtReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "clinical_status", nullable = false)
    private ClinicalStatus clinicalStatus;

    @Column(name = "was_examined", nullable = false)
    private boolean wasExamined = true;

    @Column(name = "management_plan", columnDefinition = "TEXT")
    private String managementPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_assessment", nullable = false)
    private DischargeAssessment dischargeAssessment = DischargeAssessment.NONE;

    @Column(name = "notified_next_of_kin", nullable = false)
    private boolean notifiedNextOfKin = false;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getRoundId() { return roundId; }
    public void setRoundId(String roundId) { this.roundId = roundId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getReviewedById() { return reviewedById; }
    public void setReviewedById(String reviewedById) { this.reviewedById = reviewedById; }
    public int getReviewOrder() { return reviewOrder; }
    public void setReviewOrder(int reviewOrder) { this.reviewOrder = reviewOrder; }
    public int getNewsScoreAtReview() { return newsScoreAtReview; }
    public void setNewsScoreAtReview(int newsScoreAtReview) { this.newsScoreAtReview = newsScoreAtReview; }
    public ClinicalStatus getClinicalStatus() { return clinicalStatus; }
    public void setClinicalStatus(ClinicalStatus clinicalStatus) { this.clinicalStatus = clinicalStatus; }
    public boolean isWasExamined() { return wasExamined; }
    public void setWasExamined(boolean wasExamined) { this.wasExamined = wasExamined; }
    public String getManagementPlan() { return managementPlan; }
    public void setManagementPlan(String managementPlan) { this.managementPlan = managementPlan; }
    public DischargeAssessment getDischargeAssessment() { return dischargeAssessment; }
    public void setDischargeAssessment(DischargeAssessment dischargeAssessment) { this.dischargeAssessment = dischargeAssessment; }
    public boolean isNotifiedNextOfKin() { return notifiedNextOfKin; }
    public void setNotifiedNextOfKin(boolean notifiedNextOfKin) { this.notifiedNextOfKin = notifiedNextOfKin; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
