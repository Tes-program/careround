package com.careround.shared.event.events;

public record PatientDischargeReadyEvent(String patientId, String wardId, java.time.LocalDate estimatedDischargeDate, String hospitalId, String correlationId) {}
