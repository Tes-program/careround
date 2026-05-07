package com.careround.hospital.oncall.repository;

import com.careround.hospital.oncall.entity.OnCallRotation;
import com.careround.shared.enums.OnCallRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnCallRotationRepository extends JpaRepository<OnCallRotation, String> {
    List<OnCallRotation> findByHospitalIdAndDepartmentIdAndRoleAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        String hospitalId, String departmentId, OnCallRole role, LocalDateTime now1, LocalDateTime now2);
}
