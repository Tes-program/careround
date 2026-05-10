package com.careround.hospital.hospital.dto;

import java.time.LocalDateTime;

public record HospitalResponse(
        String id,
        String name,
        String address,
        String contactEmail,
        String contactPhone,
        LocalDateTime createdAt
) {}
