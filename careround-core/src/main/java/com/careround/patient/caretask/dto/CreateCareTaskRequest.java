package com.careround.patient.caretask.dto;

import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateCareTaskRequest(
        @NotBlank String patientId,
        @NotBlank String taskType,
        @NotNull TaskSource source,
        @NotBlank String title,
        String description,
        TaskPriority priority,
        String roundId,
        @NotNull(message = "windowStart is required") LocalDateTime windowStart,
        @NotNull(message = "windowEnd is required") LocalDateTime windowEnd
) {}
