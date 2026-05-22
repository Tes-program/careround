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
        String phoneNumber,
        String address,
        String previousConditions,
        String currentMedications,
        String allergies,
        String emergencyContactName,
        String emergencyContactPhone,
        String registeredById,
        AdmissionType admissionType,
        String primaryDiagnosis,
        AcuityColor acuityColor,
        LocalDate estimatedDischargeDate,
        PatientStatus status,
        LocalDateTime admissionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
