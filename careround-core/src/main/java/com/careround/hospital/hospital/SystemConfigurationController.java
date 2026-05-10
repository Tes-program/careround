package com.careround.hospital.hospital;

import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.hospital.hospital.dto.UpdateSystemConfigRequest;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
public class SystemConfigurationController {

    private final SystemConfigurationService systemConfigurationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> get() {
        SystemConfigResponse response = systemConfigurationService
                .getByHospitalId(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> update(
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        SystemConfigResponse response = systemConfigurationService
                .update(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.ok(ApiResponse.ok("System configuration updated", response));
    }
}
