package com.careround.patient.caretask.dto;

import com.careround.patient.enums.AssignedToRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignTaskRequest(
        @NotBlank String assignedToId,
        @NotNull AssignedToRole assignedToRole
) {}
