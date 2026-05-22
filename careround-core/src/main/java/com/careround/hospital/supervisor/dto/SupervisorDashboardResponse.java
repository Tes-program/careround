package com.careround.hospital.supervisor.dto;

import java.util.List;

public record SupervisorDashboardResponse(
        String wardId,
        String wardName,
        String specialty,
        int totalBeds,
        int occupiedBeds,
        List<PatientSummary> patients,
        TaskStats taskStats,
        List<OverdueAlert> overdueAlerts,
        List<HourlyTaskCount> hourlyChart
) {}
