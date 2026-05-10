package com.careround.hospital.ward;

import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.UpdateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WardResponse>> create(
            @Valid @RequestBody CreateWardRequest request) {
        WardResponse response = wardService.create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Ward created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WardResponse>>> list() {
        List<WardResponse> response = wardService.listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WardResponse>> getById(@PathVariable String id) {
        WardResponse response = wardService.getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<WardResponse>> update(
            @PathVariable String id, @RequestBody UpdateWardRequest request) {
        WardResponse response = wardService.update(HospitalContextHolder.getHospitalId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Ward updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        wardService.delete(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Ward deleted", null));
    }
}
