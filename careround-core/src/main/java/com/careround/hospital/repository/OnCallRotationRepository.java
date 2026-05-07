package com.careround.hospital.repository;

import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.enums.OnCallRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OnCallRotationRepository extends JpaRepository<OnCallRotation, String> {

    Optional<OnCallRotation> findFirstByDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
            String departmentId, OnCallRole role, LocalDateTime now1, LocalDateTime now2);
}
