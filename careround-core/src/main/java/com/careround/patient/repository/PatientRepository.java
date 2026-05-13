package com.careround.patient.repository;

import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    List<Patient> findAllByHospitalIdAndWardIdOrderByAcuityLevelDescNewsScoreDesc(
            String hospitalId, String wardId);

    List<Patient> findAllByHospitalIdAndWardIdAndStatusOrderByNewsScoreDescAdmissionDateAsc(
            String hospitalId, String wardId, PatientStatus status);

    Optional<Patient> findByIdAndHospitalId(String id, String hospitalId);

    Optional<Patient> findByHospitalNumber(String hospitalNumber);

    List<Patient> findAllByMedicalTeamIdAndStatus(String teamId, PatientStatus status);

    long countByHospitalIdAndStatus(String hospitalId, PatientStatus status);

    long countByHospitalIdAndWardIdInAndStatus(String hospitalId, List<String> wardIds, PatientStatus status);

    long countByHospitalIdAndMedicalTeamIdInAndStatus(String hospitalId, List<String> medicalTeamIds, PatientStatus status);

    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND " +
           "(p.hospitalNumber LIKE %:q% OR p.firstName LIKE %:q% OR p.lastName LIKE %:q%)")
    List<Patient> searchByHospitalId(@Param("hospitalId") String hospitalId, @Param("q") String q);

    @Query("""
            select p
            from Patient p
            where p.hospitalId = :hospitalId
              and (:wardId is null or p.wardId = :wardId)
              and p.admissionDate between :from and :to
            order by p.admissionDate asc
            """)
    List<Patient> findAdmissionsForReport(
            @Param("hospitalId") String hospitalId,
            @Param("wardId") String wardId,
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to);
}
