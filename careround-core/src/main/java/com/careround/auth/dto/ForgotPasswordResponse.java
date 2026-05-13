package com.careround.auth.dto;

import java.time.LocalDateTime;

public record ForgotPasswordResponse(
        String resetToken,
        LocalDateTime expiresAt
) {}
