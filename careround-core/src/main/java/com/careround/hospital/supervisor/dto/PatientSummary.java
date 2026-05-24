package com.careround.hospital.supervisor.dto;

/**
 * Per-patient summary on the supervisor dashboard.
 * {@code admissionDate} is an ISO-8601 UTC string, e.g. "2026-05-20T08:30:00Z".
 */
public record PatientSummary(
        String patientId,
        String firstName,
        String lastName,
        String acuityColor,
        String admissionDate,
        String wardId
) {}
