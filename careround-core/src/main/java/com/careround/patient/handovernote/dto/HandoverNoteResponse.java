package com.careround.patient.handovernote.dto;

import java.time.LocalDateTime;

public record HandoverNoteResponse(
        String id,
        String patientId,
        String hospitalId,
        String authorId,
        String content,
        LocalDateTime createdAt
) {}
