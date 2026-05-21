package com.careround.hospital.supervisor;

import com.careround.hospital.supervisor.dto.SupervisorDashboardResponse;

public interface SupervisorDashboardService {
    SupervisorDashboardResponse getDashboard(String wardId);
}
