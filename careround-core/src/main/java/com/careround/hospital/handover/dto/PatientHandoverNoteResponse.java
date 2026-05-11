package com.careround.hospital.handover.dto;

import java.time.LocalDateTime;

public record PatientHandoverNoteResponse(
        String id,
        String handoverId,
        String patientId,
        String statusSummary,
        String outstandingTaskIds,
        boolean urgencyFlag,
        String addedById,
        LocalDateTime createdAt
) {}
