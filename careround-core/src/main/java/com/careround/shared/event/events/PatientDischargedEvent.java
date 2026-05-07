package com.careround.shared.event.events;

public record PatientDischargedEvent(String patientId, String wardId, java.util.List<String> nokIds, String hospitalId, String correlationId) {}
