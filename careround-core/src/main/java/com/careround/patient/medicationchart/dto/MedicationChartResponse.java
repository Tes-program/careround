package com.careround.patient.medicationchart.dto;

import com.careround.patient.medicationchart.enums.MedicationChartStatus;

import java.time.LocalDateTime;

public record MedicationChartResponse(
        String id,
        String patientId,
        String hospitalId,
        String prescriptionId,
        MedicationChartStatus status,
        String nurseNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
