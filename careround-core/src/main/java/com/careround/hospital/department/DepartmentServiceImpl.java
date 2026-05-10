package com.careround.hospital.department;

import com.careround.hospital.department.dto.CreateDepartmentRequest;
import com.careround.hospital.department.dto.DepartmentResponse;
import com.careround.hospital.department.dto.UpdateDepartmentRequest;
import com.careround.hospital.entity.Department;
import com.careround.hospital.repository.DepartmentRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse create(String hospitalId, CreateDepartmentRequest request) {
        Department department = new Department();
        department.setHospitalId(hospitalId);
        department.setName(request.name());
        department.setHeadOfDepartmentId(request.headOfDepartmentId());
        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(String hospitalId, String departmentId) {
        return departmentRepository.findByIdAndHospitalId(departmentId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> listByHospital(String hospitalId) {
        return departmentRepository.findAllByHospitalId(hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public DepartmentResponse update(String hospitalId, String departmentId, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findByIdAndHospitalId(departmentId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        if (request.name() != null) department.setName(request.name());
        if (request.headOfDepartmentId() != null) department.setHeadOfDepartmentId(request.headOfDepartmentId());
        return toResponse(department);
    }

    @Override
    @Transactional
    public void delete(String hospitalId, String departmentId) {
        Department department = departmentRepository.findByIdAndHospitalId(departmentId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        departmentRepository.delete(department);
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(d.getId(), d.getHospitalId(), d.getName(),
                d.getHeadOfDepartmentId(), d.getCreatedAt());
    }
}
