package com.careround.hospital.repository;

import com.careround.hospital.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    List<Department> findAllByHospitalId(String hospitalId);

    Optional<Department> findByIdAndHospitalId(String id, String hospitalId);
}
