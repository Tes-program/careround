package com.careround.hospital.shift.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignStaffRequest(
        @NotBlank String leadDoctorId,
        @NotBlank String nurseInChargeId
) {}
