package com.careround.hospital.supervisor.dto;

/**
 * An overdue medication-task alert for the supervisor dashboard.
 * {@code scheduledTime} is an ISO-8601 UTC string, e.g. "2026-05-20T10:00:00Z".
 */
public record OverdueAlert(
        String taskId,
        String patientId,
        String patientName,
        String assignedNurseId,
        String scheduledTime,
        int minutesOverdue
) {}
