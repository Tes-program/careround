package com.careround.patient.medicationtask.dto;

import com.careround.patient.medicationtask.enums.MedicationTaskStatus;

import java.time.LocalDateTime;

public record MedicationTaskResponse(
        String id,
        String medicationChartId,
        String patientId,
        String hospitalId,
        String wardId,
        String assignedNurseId,
        LocalDateTime scheduledTime,
        MedicationTaskStatus status,
        LocalDateTime completedAt,
        String completedById,
        String actualDoseGiven,
        LocalDateTime preReminderSentAt,
        LocalDateTime overdueAlertSentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String patientFirstName,
        String patientLastName,
        String bedNumber,
        String drugName,
        String dose,
        String route,
        Long minutesOverdue,
        String completedByName
) {}
