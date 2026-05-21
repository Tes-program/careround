package com.careround.hospital.supervisor.dto;

import java.time.LocalDateTime;

public record PatientSummary(
        String patientId,
        String firstName,
        String lastName,
        String acuityColor,
        LocalDateTime admissionDate,
        String wardId
) {}
