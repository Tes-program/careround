package com.careround.patient.escalation.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveEscalationRequest(
        @NotBlank String notes
) {}
