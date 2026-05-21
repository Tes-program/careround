package com.careround.hospital.hospital.dto;

public record HospitalRegistrationResponse(
        String hospitalId,
        String code,
        String adminUserId
) {}
