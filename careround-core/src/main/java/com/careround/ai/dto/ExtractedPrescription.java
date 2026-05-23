package com.careround.ai.dto;

import java.util.List;

public record ExtractedPrescription(
        String drugName,
        String dose,
        String route,
        String frequencyString,
        Integer frequencyHours,
        Integer totalDoses,
        List<String> administrationTimes
) {}
