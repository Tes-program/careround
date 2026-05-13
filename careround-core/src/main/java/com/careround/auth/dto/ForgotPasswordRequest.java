package com.careround.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank String hospitalId,
        @NotBlank @Email String email
) {}
