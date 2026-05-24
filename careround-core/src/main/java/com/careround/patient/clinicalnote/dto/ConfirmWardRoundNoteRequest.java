package com.careround.patient.clinicalnote.dto;

import com.careround.patient.prescription.dto.CreatePrescriptionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body sent by the frontend to
 * {@code POST /api/v1/patients/{patientId}/notes/confirm}.
 */
public record ConfirmWardRoundNoteRequest(
        String rawTranscription,
        @NotNull @Valid ClinicalNoteContent clinicalNote,
        @NotNull @Valid List<CreatePrescriptionRequest> prescriptions
) {}
