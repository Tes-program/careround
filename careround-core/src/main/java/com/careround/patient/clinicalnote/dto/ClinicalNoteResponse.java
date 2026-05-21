package com.careround.patient.clinicalnote.dto;

import com.careround.patient.enums.NoteType;

import java.time.LocalDateTime;

public record ClinicalNoteResponse(
        String id,
        String patientId,
        String hospitalId,
        String authorId,
        NoteType noteType,
        String content,
        String rawTranscription,
        boolean isAiGenerated,
        String aiModelUsed,
        LocalDateTime confirmedByDoctorAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
