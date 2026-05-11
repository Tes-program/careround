package com.careround.hospital.handover.dto;

import jakarta.validation.constraints.NotBlank;

public record InitiateHandoverRequest(
        @NotBlank String wardId,
        @NotBlank String outgoingShiftId,
        @NotBlank String incomingShiftId,
        String generalNotes
) {}
