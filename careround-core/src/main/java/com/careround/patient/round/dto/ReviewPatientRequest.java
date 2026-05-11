package com.careround.patient.round.dto;

import com.careround.patient.enums.ClinicalStatus;
import com.careround.patient.enums.DischargeAssessment;
import jakarta.validation.constraints.NotNull;

public record ReviewPatientRequest(
        @NotNull ClinicalStatus clinicalStatus,
        @NotNull Boolean wasExamined,
        String managementPlan,
        @NotNull DischargeAssessment dischargeAssessment,
        @NotNull Boolean notifiedNextOfKin
) {}
