package com.careround.patient.handovernote.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHandoverNoteRequest(@NotBlank String content) {}
