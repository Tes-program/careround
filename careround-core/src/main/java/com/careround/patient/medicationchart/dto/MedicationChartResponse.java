package com.careround.patient.medicationchart.dto;

import com.careround.patient.medicationchart.enums.MedicationChartStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MedicationChartResponse(
        String id,
        String patientId,
        String hospitalId,
        String prescriptionId,
        MedicationChartStatus status,
        String nurseNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String drugName,
        String dose,
        String route,
        String frequencyString,
        Integer frequencyHours,
        Integer totalDoses,
        LocalDateTime startTime,
        String confirmedById,
        String confirmedByName,
        List<AdministrationSlotResponse> administrationSlots
) {}
