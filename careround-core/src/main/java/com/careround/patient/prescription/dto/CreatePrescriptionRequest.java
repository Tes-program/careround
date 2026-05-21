package com.careround.patient.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public record CreatePrescriptionRequest(
        @NotBlank String drugName,
        @NotBlank String dose,
        @NotBlank String route,
        @NotBlank String frequencyString,
        @Positive int frequencyHours,
        @Positive int totalDoses,
        @NotNull LocalDateTime startTime,
        @NotEmpty List<LocalDateTime> administrationTimes
) {}
