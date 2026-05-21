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
        @NotNull AdmissionType admissionType,
        String primaryDiagnosis,
        String bedNumber,
        LocalDate estimatedDischargeDate
) {}
