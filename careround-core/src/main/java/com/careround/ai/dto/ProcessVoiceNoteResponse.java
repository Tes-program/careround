package com.careround.ai.dto;

import java.util.List;

public record ProcessVoiceNoteResponse(
        String rawTranscription,
        String mode,
        ClinicalNoteContent clinicalNote,
        List<ExtractedPrescription> prescriptions
) {}
