package com.careround.patient.clinicalnote.dto;

import jakarta.validation.constraints.NotBlank;

public record AmendNoteRequest(
        @NotBlank String content
) {}
