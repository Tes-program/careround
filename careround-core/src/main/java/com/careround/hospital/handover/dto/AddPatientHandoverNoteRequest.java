package com.careround.hospital.handover.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddPatientHandoverNoteRequest(
        @NotBlank String patientId,
        String statusSummary,
        String outstandingTaskIds,
        @NotNull Boolean urgencyFlag
) {}
