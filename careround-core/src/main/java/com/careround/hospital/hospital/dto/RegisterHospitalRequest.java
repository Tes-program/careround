package com.careround.hospital.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterHospitalRequest(
        @NotBlank @Size(max = 255) String name,
        String address,
        @NotBlank @Email String contactEmail,
        String contactPhone,
        @Size(max = 8) String code,
        @NotBlank String adminFirstName,
        @NotBlank String adminLastName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8) String adminPassword
) {}
