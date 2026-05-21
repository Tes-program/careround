package com.careround.patient.medicationtask.dto;

import java.util.List;

public record TaskListResponse(
        List<MedicationTaskResponse> overdue,
        List<MedicationTaskResponse> dueSoon,
        List<MedicationTaskResponse> upcoming
) {}
