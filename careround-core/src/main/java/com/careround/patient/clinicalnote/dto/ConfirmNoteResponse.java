package com.careround.patient.clinicalnote.dto;

import java.util.List;

public record ConfirmNoteResponse(
        String noteId,
        List<String> prescriptionIds
) {}
