package com.careround.hospital.department;

import com.careround.hospital.department.dto.CreateDepartmentRequest;
import com.careround.hospital.department.dto.DepartmentResponse;
import com.careround.hospital.department.dto.UpdateDepartmentRequest;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = departmentService.create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Department created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> list() {
        List<DepartmentResponse> response = departmentService.listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable String id) {
        DepartmentResponse response = departmentService.getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable String id, @RequestBody UpdateDepartmentRequest request) {
        DepartmentResponse response = departmentService.update(HospitalContextHolder.getHospitalId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Department updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        departmentService.delete(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Department deleted", null));
    }
}
