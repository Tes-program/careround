package com.careround.hospital.ward;

import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.UpdateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wards")
@RequiredArgsConstructor
@Tag(name = "Wards", description = "Hospital ward management")
public class WardController {

    private final WardService wardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create ward", description = "Creates a ward for the authenticated hospital.")
    public ResponseEntity<ApiResponse<WardResponse>> create(
            @Valid @RequestBody CreateWardRequest request) {
        WardResponse response = wardService.create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Ward created", response));
    }

    @GetMapping
    @Operation(summary = "List wards", description = "Returns wards for the authenticated hospital.")
    public ResponseEntity<ApiResponse<List<WardResponse>>> list() {
        List<WardResponse> response = wardService.listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ward", description = "Returns a ward by id.")
    public ResponseEntity<ApiResponse<WardResponse>> getById(@PathVariable String id) {
        WardResponse response = wardService.getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/dashboard")
    @Operation(
            summary = "Get ward dashboard",
            description = "Returns ward-level operational summary: patients, beds, current shift, open tasks, overdue tasks, open escalations, active rounds, and recent handover status."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @Parameter(description = "Ward id") @PathVariable String id) {
        Map<String, Object> response = wardService.getDashboard(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARD_SUPERVISOR')")
    @Operation(summary = "Update ward", description = "Updates a ward by id.")
    public ResponseEntity<ApiResponse<WardResponse>> update(
            @PathVariable String id, @RequestBody UpdateWardRequest request) {
        WardResponse response = wardService.update(HospitalContextHolder.getHospitalId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Ward updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete ward", description = "Deletes a ward by id.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        wardService.delete(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Ward deleted", null));
    }
}
