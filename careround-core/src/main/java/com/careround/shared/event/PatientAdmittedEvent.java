package com.careround.shared.event;

public record PatientAdmittedEvent(
        String hospitalId,
        String patientId,
        String wardId,
        String medicalTeamId,
        String admittingConsultantId,
        String correlationId
) {}
