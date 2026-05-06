package com.careround.common.repository;

import com.careround.common.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, String> {
    List<Department> findByHospitalId(String hospitalId);
    Optional<Department> findByIdAndHospitalId(String id, String hospitalId);
    boolean existsByNameAndHospitalId(String name, String hospitalId);
}
