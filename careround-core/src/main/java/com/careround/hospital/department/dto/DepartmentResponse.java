package com.careround.hospital.department.dto;

import java.time.LocalDateTime;

public record DepartmentResponse(
        String id,
        String hospitalId,
        String name,
        String headOfDepartmentId,
        LocalDateTime createdAt
) {}
