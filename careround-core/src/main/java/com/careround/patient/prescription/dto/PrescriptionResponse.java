package com.careround.patient.prescription.dto;

import com.careround.patient.prescription.enums.PrescriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionResponse(
        String id,
        String patientId,
        String hospitalId,
        String clinicalNoteId,
        String drugName,
        String dose,
        String route,
        String frequencyString,
        int frequencyHours,
        int totalDoses,
        LocalDateTime startTime,
        List<LocalDateTime> administrationTimes,
        String confirmedById,
        LocalDateTime confirmedAt,
        PrescriptionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
