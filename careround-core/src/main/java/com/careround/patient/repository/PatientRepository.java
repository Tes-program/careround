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

    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND " +
           "(p.hospitalNumber LIKE %:q% OR p.firstName LIKE %:q% OR p.lastName LIKE %:q%)")
    List<Patient> searchByHospitalId(@Param("hospitalId") String hospitalId, @Param("q") String q);
}
