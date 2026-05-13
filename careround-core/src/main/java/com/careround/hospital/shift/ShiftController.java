package com.careround.hospital.shift;

import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Ward shift staff assignment and current-shift lookup")
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    @Operation(
            summary = "List ward shifts",
            description = "Returns generated shifts for a ward and date-time range, optionally filtered by status."
    )
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> listShifts(
            @Parameter(description = "Ward id") @RequestParam String wardId,
            @Parameter(description = "Optional shift status filter") @RequestParam(required = false) ShiftStatus status,
            @Parameter(description = "Range start, ISO date-time") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Range end, ISO date-time") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.listShifts(
                HospitalContextHolder.getHospitalId(), wardId, status, from, to)));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARD_SUPERVISOR')")
    @Operation(summary = "Assign shift staff", description = "Assigns the lead doctor and nurse in charge, then activates the shift.")
    public ResponseEntity<ApiResponse<ShiftResponse>> assignStaff(
            @PathVariable String id,
            @Valid @RequestBody AssignStaffRequest request) {
        ShiftResponse response = shiftService.assignStaff(
                HospitalContextHolder.getHospitalId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Staff assigned", response));
    }

    @GetMapping("/current/{wardId}")
    @Operation(summary = "Get current ward shift", description = "Returns the active shift for a ward.")
    public ResponseEntity<ApiResponse<ShiftResponse>> getCurrentShift(@PathVariable String wardId) {
        ShiftResponse response = shiftService.getCurrentShift(
                HospitalContextHolder.getHospitalId(), wardId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
