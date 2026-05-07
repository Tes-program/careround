package com.careround.shared.event.events;

public record ShiftActivatedEvent(String shiftId, String wardId, String leadDoctorId, String nurseInChargeId, String hospitalId, String correlationId) {}
