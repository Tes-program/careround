package com.careround.hospital.oncall.dto;

import com.careround.hospital.enums.OnCallRole;

import java.time.LocalDateTime;

public record OnCallRotationResponse(
        String id,
        String hospitalId,
        String departmentId,
        String wardId,
        String doctorId,
        OnCallRole role,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt
) {}
