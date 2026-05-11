package com.careround.patient.patient.dto;

import com.careround.patient.enums.PatientStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePatientStatusRequest(
        @NotNull PatientStatus status
) {}
