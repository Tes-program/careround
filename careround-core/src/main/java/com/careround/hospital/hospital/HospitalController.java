package com.careround.hospital.hospital;

import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
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
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<HospitalResponse>>> listHospitals() {
        return ResponseEntity.ok(ApiResponse.ok(hospitalService.listAll()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<HospitalResponse>> getMyHospital() {
        HospitalResponse response = hospitalService.getById(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
