package com.careround.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceTokenRequest(@NotBlank String deviceToken) {}
