package com.careround.patient.vitals.dto;

import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.ConsciousnessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalsResponse(
        String id,
        String patientId,
        String hospitalId,
        String recordedById,
        Integer heartRate,
        Integer respiratoryRate,
        Integer systolicBP,
        BigDecimal oxygenSaturation,
        BigDecimal temperature,
        ConsciousnessLevel consciousnessLevel,
        int computedScore,
        AcuityColor acuityColor,
        LocalDateTime recordedAt
) {}
