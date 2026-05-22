package com.careround.hospital.supervisor.dto;

import java.time.LocalDateTime;

public record OverdueAlert(
        String taskId,
        String patientId,
        String patientName,
        String assignedNurseId,
        LocalDateTime scheduledTime,
        long minutesOverdue
) {}
