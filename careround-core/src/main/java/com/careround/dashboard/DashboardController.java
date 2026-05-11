package com.careround.dashboard;

import com.careround.auth.enums.UserRole;
import com.careround.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.currentUserDashboard()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> admin() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.ADMIN)));
    }

    @GetMapping("/consultant")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultant() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.CONSULTANT)));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> doctor() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.currentUserDashboard()));
    }

    @GetMapping("/nurse")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> nurse() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.NURSE)));
    }

    @GetMapping("/ward-supervisor")
    @PreAuthorize("hasRole('WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> wardSupervisor() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.WARD_SUPERVISOR)));
    }
}
