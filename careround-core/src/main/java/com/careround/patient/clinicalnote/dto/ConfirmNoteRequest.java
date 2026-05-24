package com.careround.patient.clinicalnote.dto;

import com.careround.patient.enums.NoteType;
import com.careround.patient.prescription.dto.CreatePrescriptionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ConfirmNoteRequest(
        @NotBlank String patientId,
        @NotNull NoteType noteType,
        @NotBlank String content,
        String rawTranscription,
        boolean isAiGenerated,
        String aiModelUsed,
        boolean extractPrescriptionsFromAi,
        LocalDateTime defaultPrescriptionStartTime,
        @NotNull @Valid List<CreatePrescriptionRequest> prescriptions
) {}
