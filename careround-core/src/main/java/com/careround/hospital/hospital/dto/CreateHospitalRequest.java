package com.careround.hospital.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateHospitalRequest(
        @NotBlank String name,
        String address,
        @NotBlank @Email String contactEmail,
        String contactPhone
) {}
