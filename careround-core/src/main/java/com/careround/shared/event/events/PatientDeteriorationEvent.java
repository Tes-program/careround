package com.careround.shared.event.events;

public record PatientDeteriorationEvent(String patientId, String wardId, int newsScore, String onCallDoctorId, com.careround.shared.enums.EscalationSeverity severity, String hospitalId, String correlationId) {}
