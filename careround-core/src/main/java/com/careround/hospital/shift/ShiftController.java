package com.careround.hospital.shift;

import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<ShiftResponse>> assignStaff(
            @PathVariable String id,
            @Valid @RequestBody AssignStaffRequest request) {
        ShiftResponse response = shiftService.assignStaff(
                HospitalContextHolder.getHospitalId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Staff assigned", response));
    }

    @GetMapping("/current/{wardId}")
    public ResponseEntity<ApiResponse<ShiftResponse>> getCurrentShift(@PathVariable String wardId) {
        ShiftResponse response = shiftService.getCurrentShift(
                HospitalContextHolder.getHospitalId(), wardId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
