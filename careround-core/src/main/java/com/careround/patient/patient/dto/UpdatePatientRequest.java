package com.careround.patient.patient.dto;

import com.careround.patient.enums.AdmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdatePatientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String gender,
        @NotNull AdmissionType admissionType,
        String wardId,
        String bedNumber,
        String phoneNumber,
        String address,
        String previousConditions,
        String currentMedications,
        String allergies,
        String emergencyContactName,
        String emergencyContactPhone
) {}