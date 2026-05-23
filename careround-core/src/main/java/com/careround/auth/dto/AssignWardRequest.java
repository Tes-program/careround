package com.careround.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignWardRequest(
        @NotBlank String wardId
) {}
