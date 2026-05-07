package com.careround.patient.entity;

import com.careround.patient.enums.ClinicalStatus;
import com.careround.patient.enums.DischargeAssessment;
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
@Table(name = "patient_round_review")
@Getter
@Setter
@NoArgsConstructor
public class PatientRoundReview extends BaseEntity {

    @Column(name = "round_id", nullable = false, length = 36)
    private String roundId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "reviewed_by_id", nullable = false, length = 36)
    private String reviewedById;

    @Column(name = "review_order", nullable = false)
    private int reviewOrder;

    @Column(name = "news_score_at_review")
    private Integer newsScoreAtReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "clinical_status", nullable = false, length = 20)
    private ClinicalStatus clinicalStatus;

    @Column(name = "was_examined", nullable = false)
    private boolean wasExamined = false;

    @Column(name = "management_plan", columnDefinition = "TEXT")
    private String managementPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_assessment", nullable = false, length = 20)
    private DischargeAssessment dischargeAssessment = DischargeAssessment.NONE;

    @Column(name = "notified_next_of_kin", nullable = false)
    private boolean notifiedNextOfKin = false;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
}
