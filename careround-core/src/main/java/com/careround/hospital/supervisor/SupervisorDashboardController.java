package com.careround.hospital.supervisor;

import com.careround.hospital.supervisor.dto.SupervisorDashboardResponse;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/supervisor")
@RequiredArgsConstructor
@Tag(name = "Supervisor", description = "Supervisor ward dashboard")
public class SupervisorDashboardController {

    private final SupervisorDashboardService supervisorDashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Operation(summary = "Supervisor dashboard", description = "Returns ward patients sorted by acuity for the supervisor view.")
    public ResponseEntity<ApiResponse<SupervisorDashboardResponse>> getDashboard(
            @RequestParam String wardId) {
        SupervisorDashboardResponse response = supervisorDashboardService.getDashboard(wardId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
