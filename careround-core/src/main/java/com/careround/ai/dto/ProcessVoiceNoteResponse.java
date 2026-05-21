package com.careround.ai.dto;

import java.util.List;

public record ProcessVoiceNoteResponse(
        String rawTranscription,
        ClinicalNoteContent clinicalNote,
        List<ExtractedPrescription> prescriptions
) {}
