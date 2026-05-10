package com.careround.hospital.department.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(
        @NotBlank String name,
        String headOfDepartmentId
) {}
