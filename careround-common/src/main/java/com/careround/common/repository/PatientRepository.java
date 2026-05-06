package com.careround.common.repository;

import com.careround.common.entity.Patient;
import com.careround.common.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {
    Optional<Patient> findByHospitalNumber(String hospitalNumber);
    Optional<Patient> findByIdAndHospitalId(String id, String hospitalId);

    /** Ordered by clinical priority: acuity DESC then NEWS score DESC */
    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.wardId = :wardId " +
           "AND p.status != 'DISCHARGED' " +
           "ORDER BY p.acuityLevel DESC, p.newsScore DESC")
    List<Patient> findByHospitalIdAndWardIdOrderByPriority(
            @Param("hospitalId") String hospitalId,
            @Param("wardId") String wardId);

    List<Patient> findByHospitalIdAndMedicalTeamId(String hospitalId, String medicalTeamId);
    List<Patient> findByHospitalIdAndStatus(String hospitalId, PatientStatus status);

    @Query("SELECT p FROM Patient p WHERE p.medicalTeamId = :teamId AND p.status != 'DISCHARGED' " +
           "ORDER BY p.acuityLevel DESC, p.newsScore DESC")
    List<Patient> findActiveByMedicalTeamIdOrderByPriority(@Param("teamId") String teamId);
}
