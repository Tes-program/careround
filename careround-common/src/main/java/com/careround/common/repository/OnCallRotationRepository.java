package com.careround.common.repository;

import com.careround.common.entity.OnCallRotation;
import com.careround.common.enums.OnCallRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OnCallRotationRepository extends JpaRepository<OnCallRotation, String> {

    @Query("SELECT r FROM OnCallRotation r WHERE r.departmentId = :departmentId " +
           "AND r.role = :role AND r.startTime <= :now AND r.endTime > :now")
    Optional<OnCallRotation> findCurrentByDepartmentAndRole(
            @Param("departmentId") String departmentId,
            @Param("role") OnCallRole role,
            @Param("now") LocalDateTime now);

    List<OnCallRotation> findByDepartmentId(String departmentId);
    List<OnCallRotation> findByHospitalId(String hospitalId);

    @Query("SELECT r FROM OnCallRotation r " +
           "JOIN Department d ON d.id = r.departmentId " +
           "WHERE r.hospitalId = :hospitalId AND LOWER(d.name) = LOWER('General Medicine') " +
           "AND r.role = 'CONSULTANT_ON_CALL' AND r.startTime <= :now AND r.endTime > :now")
    Optional<OnCallRotation> findGeneralMedicineConsultantOnCall(
            @Param("hospitalId") String hospitalId,
            @Param("now") LocalDateTime now);
}
