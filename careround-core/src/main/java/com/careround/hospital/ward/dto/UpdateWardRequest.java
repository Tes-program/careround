package com.careround.hospital.ward.dto;

public record UpdateWardRequest(
        String name,
        String specialty,
        Integer totalBeds,
        String supervisorId
) {}
