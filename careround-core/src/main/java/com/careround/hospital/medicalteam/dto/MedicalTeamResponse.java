package com.careround.hospital.medicalteam.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MedicalTeamResponse(
        String id,
        String hospitalId,
        String name,
        String consultantId,
        String departmentId,
        LocalDateTime createdAt,
        List<String> wardIds
) {}
