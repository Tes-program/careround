package com.careround.patient.nextofkin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationConsentRequest(
        @NotNull Boolean consent
) {}
