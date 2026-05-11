package com.careround.patient.vitals.dto;

import com.careround.patient.enums.ConsciousnessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalsResponse(
        String id,
        String patientId,
        String recordedById,
        int heartRate,
        int respiratoryRate,
        int systolicBP,
        BigDecimal oxygenSaturation,
        BigDecimal temperature,
        ConsciousnessLevel consciousnessLevel,
        int newsScore,
        LocalDateTime recordedAt
) {}
