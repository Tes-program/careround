package com.careround.hospital.hospital;

import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
@Tag(name = "Hospitals", description = "Hospital tenant lookup endpoints")
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
            summary = "List all hospital tenants",
            description = "Returns all provisioned hospitals. This endpoint is restricted to platform administrators."
    )
    public ResponseEntity<ApiResponse<List<HospitalResponse>>> listHospitals() {
        return ResponseEntity.ok(ApiResponse.ok(hospitalService.listAll()));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get the current user's hospital",
            description = "Returns the hospital tenant associated with the authenticated tenant user's JWT."
    )
    public ResponseEntity<ApiResponse<HospitalResponse>> getMyHospital() {
        HospitalResponse response = hospitalService.getById(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
