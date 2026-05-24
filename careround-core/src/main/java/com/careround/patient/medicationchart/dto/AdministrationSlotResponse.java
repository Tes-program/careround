package com.careround.patient.medicationchart.dto;

import com.careround.patient.medicationtask.enums.MedicationTaskStatus;

import java.time.LocalDateTime;

public record AdministrationSlotResponse(
        String taskId,
        LocalDateTime scheduledTime,
        MedicationTaskStatus taskStatus,
        LocalDateTime completedAt,
        String completedByName,
        String actualDoseGiven
) {}
