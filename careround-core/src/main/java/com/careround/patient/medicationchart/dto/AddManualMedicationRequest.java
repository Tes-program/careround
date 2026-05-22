package com.careround.patient.medicationchart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AddManualMedicationRequest(
        @NotBlank String drugName,
        @NotBlank String dose,
        @NotBlank String route,
        @NotBlank String frequencyString,
        @NotNull Integer frequencyHours,
        @NotNull Integer totalDoses,
        @NotNull LocalDateTime startTime,
        @NotEmpty List<LocalDateTime> administrationTimes
) {}
