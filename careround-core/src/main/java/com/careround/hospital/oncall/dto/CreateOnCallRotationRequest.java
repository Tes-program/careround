package com.careround.hospital.oncall.dto;

import com.careround.hospital.enums.OnCallRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateOnCallRotationRequest(
        @NotBlank String departmentId,
        String wardId,
        @NotBlank String doctorId,
        @NotNull OnCallRole role,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {}
