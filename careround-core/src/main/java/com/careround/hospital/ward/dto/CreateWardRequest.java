package com.careround.hospital.ward.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateWardRequest(
        @NotBlank String name,
        String specialty,
        @Min(0) int totalBeds,
        String supervisorId
) {}
