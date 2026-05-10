package com.careround.hospital.medicalteam.dto;

import java.time.LocalDateTime;

public record MedicalTeamResponse(
        String id,
        String hospitalId,
        String name,
        String consultantId,
        String departmentId,
        LocalDateTime createdAt
) {}
