package com.careround.patient.patient.dto;

import com.careround.patient.enums.AdmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdmitPatientRequest(
        String wardId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String gender,
        @NotBlank String hospitalNumber,
        String phoneNumber,
        String address,
        String previousConditions,
        String currentMedications,
        String allergies,
        String emergencyContactName,
        String emergencyContactPhone,
        @NotNull AdmissionType admissionType,
        String primaryDiagnosis,
        String bedNumber,
        LocalDate estimatedDischargeDate
) {}
