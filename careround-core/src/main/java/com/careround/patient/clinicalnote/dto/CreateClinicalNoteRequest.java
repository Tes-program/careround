package com.careround.patient.clinicalnote.dto;

import com.careround.patient.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClinicalNoteRequest(
        @NotBlank String patientId,
        @NotNull NoteType noteType,
        @NotBlank String content,
        String patientRoundReviewId,
        String vitalsId
) {
    public CreateClinicalNoteRequest(String patientId, NoteType noteType, String content, String patientRoundReviewId) {
        this(patientId, noteType, content, patientRoundReviewId, null);
    }
}
