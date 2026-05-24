package com.careround.patient.clinicalnote.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * SOAP-structured clinical note content sent by the frontend
 * for a ward-round confirmation.
 */
public record ClinicalNoteContent(
        @NotBlank String subjective,
        @NotBlank String objective,
        @NotBlank String assessment,
        @NotBlank String plan
) {}
