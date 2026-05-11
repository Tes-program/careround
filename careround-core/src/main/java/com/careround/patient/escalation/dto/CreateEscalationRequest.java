package com.careround.patient.escalation.dto;

import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationTrigger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEscalationRequest(
        @NotBlank String patientId,
        @NotNull EscalationSeverity severity,
        @NotNull EscalationTrigger triggerType,
        String notes
) {}
