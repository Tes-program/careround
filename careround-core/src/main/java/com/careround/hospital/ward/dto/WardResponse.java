package com.careround.hospital.ward.dto;

import java.time.LocalDateTime;

public record WardResponse(
        String id,
        String hospitalId,
        String name,
        String specialty,
        int totalBeds,
        String supervisorId,
        LocalDateTime createdAt
) {}
