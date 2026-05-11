package com.careround.shared.event;

import java.time.LocalDate;

public record PatientDischargeReadyEvent(
        String hospitalId,
        String patientId,
        String wardId,
        String medicalTeamId,
        LocalDate estimatedDischargeDate,
        String correlationId
) {}
