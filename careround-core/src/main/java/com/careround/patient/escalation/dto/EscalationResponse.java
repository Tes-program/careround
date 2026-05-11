package com.careround.patient.escalation.dto;

import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.EscalationTrigger;

import java.time.LocalDateTime;

public record EscalationResponse(
        String id,
        String patientId,
        String hospitalId,
        String triggeredById,
        String assignedToId,
        EscalationTrigger triggerType,
        EscalationSeverity severity,
        EscalationStatus status,
        String notes,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
