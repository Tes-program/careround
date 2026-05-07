package com.careround.shared.event.events;

public record PatientAdmittedEvent(String patientId, String wardId, String medicalTeamId, String hospitalId, String correlationId) {}
