package com.careround.hospital.shiftschedule;

import com.careround.hospital.shiftschedule.dto.CreateShiftScheduleRequest;
import com.careround.hospital.shiftschedule.dto.ShiftScheduleResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shift-schedules")
@RequiredArgsConstructor
@Tag(name = "Shift Schedules", description = "Recurring shift schedule management")
public class ShiftScheduleController {

    private final ShiftScheduleService shiftScheduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create shift schedule", description = "Creates a recurring ward or hospital shift schedule.")
    public ResponseEntity<ApiResponse<ShiftScheduleResponse>> create(
            @Valid @RequestBody CreateShiftScheduleRequest request) {
        ShiftScheduleResponse response = shiftScheduleService
                .create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Shift schedule created", response));
    }

    @GetMapping
    @Operation(summary = "List active shift schedules", description = "Returns active shift schedules for the authenticated hospital.")
    public ResponseEntity<ApiResponse<List<ShiftScheduleResponse>>> listActive() {
        List<ShiftScheduleResponse> response = shiftScheduleService
                .listActive(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shift schedule", description = "Returns a shift schedule by id.")
    public ResponseEntity<ApiResponse<ShiftScheduleResponse>> getById(@PathVariable String id) {
        ShiftScheduleResponse response = shiftScheduleService
                .getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate shift schedule", description = "Deactivates a recurring shift schedule.")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String id) {
        shiftScheduleService.deactivate(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Shift schedule deactivated", null));
    }
}
