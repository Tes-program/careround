package com.careround.hospital.medicalteam.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMedicalTeamRequest(
        @NotBlank String name,
        @NotBlank String departmentId,
        String consultantId
) {}
