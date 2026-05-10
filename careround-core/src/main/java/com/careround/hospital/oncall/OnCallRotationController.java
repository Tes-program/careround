package com.careround.hospital.oncall;

import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.oncall.dto.CreateOnCallRotationRequest;
import com.careround.hospital.oncall.dto.OnCallRotationResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
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
public class OnCallRotationController {

    private final OnCallRotationService onCallRotationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OnCallRotationResponse>> create(
            @Valid @RequestBody CreateOnCallRotationRequest request) {
        OnCallRotationResponse response = onCallRotationService
                .create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("On-call rotation created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OnCallRotationResponse>>> list() {
        List<OnCallRotationResponse> response = onCallRotationService
                .listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OnCallRotationResponse>> getById(@PathVariable String id) {
        OnCallRotationResponse response = onCallRotationService
                .getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/current")
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
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        onCallRotationService.delete(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("On-call rotation deleted", null));
    }
}
