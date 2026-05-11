package com.careround.patient.patient.dto;

import com.careround.patient.enums.AcuityLevel;
import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        String id,
        String wardId,
        String medicalTeamId,
        String admittingConsultantId,
        String firstName,
        String lastName,
        String hospitalNumber,
        LocalDate dateOfBirth,
        String gender,
        String bedNumber,
        AdmissionType admissionType,
        String primaryDiagnosis,
        String specialtyRequired,
        AcuityLevel acuityLevel,
        int newsScore,
        boolean isDischargeReady,
        LocalDate estimatedDischargeDate,
        PatientStatus status,
        LocalDateTime admissionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
