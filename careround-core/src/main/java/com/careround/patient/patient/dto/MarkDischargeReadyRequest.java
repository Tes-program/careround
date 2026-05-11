package com.careround.patient.patient.dto;

import java.time.LocalDate;

public record MarkDischargeReadyRequest(
        LocalDate estimatedDischargeDate
) {}
