package com.careround.hospital.oncall;

import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.oncall.dto.CreateOnCallRotationRequest;
import com.careround.hospital.oncall.dto.OnCallRotationResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oncall")
@RequiredArgsConstructor
@Tag(name = "On-Call Rotations", description = "Registrar and consultant on-call rotation management")
public class OnCallRotationController {

    private final OnCallRotationService onCallRotationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create on-call rotation", description = "Creates an on-call rotation for a doctor.")
    public ResponseEntity<ApiResponse<OnCallRotationResponse>> create(
            @Valid @RequestBody CreateOnCallRotationRequest request) {
        OnCallRotationResponse response = onCallRotationService
                .create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("On-call rotation created", response));
    }

    @GetMapping
    @Operation(summary = "List on-call rotations", description = "Returns on-call rotations for the authenticated hospital.")
    public ResponseEntity<ApiResponse<List<OnCallRotationResponse>>> list() {
        List<OnCallRotationResponse> response = onCallRotationService
                .listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get on-call rotation", description = "Returns an on-call rotation by id.")
    public ResponseEntity<ApiResponse<OnCallRotationResponse>> getById(@PathVariable String id) {
        OnCallRotationResponse response = onCallRotationService
                .getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current on-call doctor", description = "Returns the current on-call rotation for a department and role.")
    public ResponseEntity<ApiResponse<OnCallRotationResponse>> getCurrent(
            @RequestParam String departmentId,
            @RequestParam OnCallRole role) {
        return onCallRotationService
                .getCurrentOnCall(HospitalContextHolder.getHospitalId(), departmentId, role)
                .map(r -> ResponseEntity.ok(ApiResponse.ok(r)))
                .orElse(ResponseEntity.ok(ApiResponse.ok(null)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete on-call rotation", description = "Deletes an on-call rotation by id.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        onCallRotationService.delete(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("On-call rotation deleted", null));
    }
}
