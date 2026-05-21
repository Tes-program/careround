package com.careround.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ExtractedPrescription(
        String drugName,
        String dose,
        String route,
        String frequencyString,
        int frequencyHours,
        int totalDoses,
        List<LocalDateTime> administrationTimes
) {}
