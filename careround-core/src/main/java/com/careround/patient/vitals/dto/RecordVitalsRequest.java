package com.careround.patient.vitals.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecordVitalsRequest(
        @NotNull @Min(20) @Max(300) Integer pulse,
        @NotNull @Min(50) @Max(300) Integer systolicBp,
        @Min(20) @Max(200) Integer diastolicBp,
        @NotNull @Min(1) @Max(70) Integer respiratoryRate,
        @NotNull @DecimalMin("25.0") @DecimalMax("45.0") BigDecimal temperature,
        @NotNull @DecimalMin("50.0") @DecimalMax("100.0") BigDecimal spo2,
        String note
) {}
