package com.careround.patient.round.dto;

import com.careround.patient.enums.ClinicalStatus;
import com.careround.patient.enums.DischargeAssessment;

import java.time.LocalDateTime;

public record PatientRoundReviewResponse(
        String id,
        String roundId,
        String patientId,
        String reviewedById,
        int reviewOrder,
        Integer newsScoreAtReview,
        ClinicalStatus clinicalStatus,
        boolean wasExamined,
        String managementPlan,
        DischargeAssessment dischargeAssessment,
        boolean notifiedNextOfKin,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {}
