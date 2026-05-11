package com.careround.patient.patient.dto;

import com.careround.patient.enums.AdmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdmitPatientRequest(
        @NotBlank String wardId,
        @NotBlank String medicalTeamId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String gender,
        @NotBlank String hospitalNumber,
        @NotNull AdmissionType admissionType,
        @NotBlank String primaryDiagnosis,
        @NotBlank String specialtyRequired,
        String admittingConsultantId,
        LocalDate estimatedDischargeDate
) {}
