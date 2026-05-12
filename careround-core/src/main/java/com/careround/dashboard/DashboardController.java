package com.careround.dashboard;

import com.careround.auth.enums.UserRole;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dashboards", description = "Role-specific operational dashboard summaries")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/me")
    @Operation(
            summary = "Get the current user's dashboard",
            description = "Returns the role-appropriate dashboard summary for the authenticated tenant user."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> me() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.currentUserDashboard()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get the admin dashboard",
            description = "Returns hospital administration metrics for tenant admins."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> admin() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.ADMIN)));
    }

    @GetMapping("/consultant")
    @PreAuthorize("hasRole('CONSULTANT')")
    @Operation(
            summary = "Get the consultant dashboard",
            description = "Returns consultant-focused clinical and team summary metrics."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultant() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.CONSULTANT)));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR')")
    @Operation(
            summary = "Get the doctor dashboard",
            description = "Returns the authenticated doctor's dashboard, scoped by their role and hospital."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> doctor() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.currentUserDashboard()));
    }

    @GetMapping("/nurse")
    @PreAuthorize("hasRole('NURSE')")
    @Operation(
            summary = "Get the nurse dashboard",
            description = "Returns ward-care and task metrics for nurses."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> nurse() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.NURSE)));
    }

    @GetMapping("/ward-supervisor")
    @PreAuthorize("hasRole('WARD_SUPERVISOR')")
    @Operation(
            summary = "Get the ward-supervisor dashboard",
            description = "Returns shift, ward, patient, and escalation metrics for the authenticated ward supervisor."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> wardSupervisor() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboardForRole(UserRole.WARD_SUPERVISOR)));
    }
}
