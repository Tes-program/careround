package com.careround.patient.vitals.dto;

import com.careround.patient.enums.VhiStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalsResponse(
        String id,
        String patientId,
        String hospitalId,
        String recordedById,
        Integer pulse,
        Integer systolicBp,
        Integer diastolicBp,
        Integer respiratoryRate,
        BigDecimal temperature,
        BigDecimal spo2,
        int vhiScore,
        VhiStatus vhiStatus,
        LocalDateTime recordedAt
) {}
