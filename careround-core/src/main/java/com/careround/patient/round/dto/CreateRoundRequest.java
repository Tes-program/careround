package com.careround.patient.round.dto;

import com.careround.patient.enums.RoundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateRoundRequest(
        @NotBlank String wardId,
        @NotBlank String medicalTeamId,
        @NotNull RoundType roundType,
        @NotBlank String leadDoctorId,
        LocalDateTime scheduledTime,
        String teamMembers
) {}
