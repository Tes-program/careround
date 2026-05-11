package com.careround.patient.caretask.dto;

import com.careround.patient.enums.AssignedToRole;
import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskSource;
import com.careround.patient.enums.TaskStatus;

import java.time.LocalDateTime;

public record CareTaskResponse(
        String id,
        String hospitalId,
        String patientId,
        String wardId,
        String roundId,
        String createdById,
        String assignedToId,
        AssignedToRole assignedToRole,
        String taskType,
        TaskSource source,
        String title,
        String description,
        TaskPriority priority,
        LocalDateTime windowStart,
        LocalDateTime windowEnd,
        TaskStatus status,
        String completedById,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
