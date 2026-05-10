package com.careround.hospital.department;

import com.careround.hospital.department.dto.CreateDepartmentRequest;
import com.careround.hospital.department.dto.DepartmentResponse;
import com.careround.hospital.department.dto.UpdateDepartmentRequest;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(String hospitalId, CreateDepartmentRequest request);
    DepartmentResponse getById(String hospitalId, String departmentId);
    List<DepartmentResponse> listByHospital(String hospitalId);
    DepartmentResponse update(String hospitalId, String departmentId, UpdateDepartmentRequest request);
    void delete(String hospitalId, String departmentId);
}
