package com.careround.hospital.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateHospitalRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank @Email String contactEmail,
        @NotBlank String contactPhone
) {}
