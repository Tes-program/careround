package com.careround.hospital.department.dto;

public record UpdateDepartmentRequest(
        String name,
        String headOfDepartmentId
) {}
