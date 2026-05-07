package com.careround.shared.event.events;

public record ShiftCreatedEvent(String shiftId, String wardId, com.careround.shared.enums.ShiftType shiftType, String scheduleId, String hospitalId, String correlationId) {}
