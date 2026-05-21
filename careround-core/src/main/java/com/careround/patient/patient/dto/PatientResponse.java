package com.careround.patient.patient.dto;

import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        String id,
        String hospitalId,
        String wardId,
        String firstName,
        String lastName,
        String hospitalNumber,
        LocalDate dateOfBirth,
        String gender,
        String bedNumber,
        AdmissionType admissionType,
        String primaryDiagnosis,
        AcuityColor acuityColor,
        LocalDate estimatedDischargeDate,
        PatientStatus status,
        LocalDateTime admissionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
