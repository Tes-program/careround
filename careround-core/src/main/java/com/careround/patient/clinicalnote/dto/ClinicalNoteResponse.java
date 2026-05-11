package com.careround.patient.clinicalnote.dto;

import com.careround.patient.enums.NoteType;

import java.time.LocalDateTime;

public record ClinicalNoteResponse(
        String id,
        String patientId,
        String patientRoundReviewId,
        String authorId,
        NoteType noteType,
        String content,
        boolean isAmended,
        String amendedById,
        LocalDateTime amendedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
