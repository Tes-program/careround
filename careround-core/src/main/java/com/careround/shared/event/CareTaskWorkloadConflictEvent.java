package com.careround.shared.event;

import java.time.LocalDateTime;

public record CareTaskWorkloadConflictEvent(
        String hospitalId,
        String taskId,
        String wardId,
        String patientId,
        String assignedNurseId,
        String wardSupervisorId,
        LocalDateTime windowStart,
        LocalDateTime windowEnd,
        String reason,
        String correlationId
) {}
