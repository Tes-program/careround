package com.careround.hospital.medicalteam.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignWardRequest(
        @NotBlank String wardId
) {
}
