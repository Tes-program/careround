package com.careround.patient.prescription.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdatePrescriptionRequest(
        String drugName,
        String dose,
        String route,
        String frequencyString,
        Integer frequencyHours,
        Integer totalDoses,
        LocalDateTime startTime,
        List<LocalDateTime> administrationTimes
) {}
