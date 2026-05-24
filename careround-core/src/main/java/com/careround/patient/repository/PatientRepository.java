package com.careround.patient.repository;

import com.careround.patient.entity.Patient;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    Optional<Patient> findByIdAndHospitalId(String id, String hospitalId);

    Optional<Patient> findByHospitalNumber(String hospitalNumber);

    List<Patient> findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(
            String hospitalId, String wardId);

    List<Patient> findAllByHospitalIdAndWardIdAndStatusOrderByAdmissionDateAsc(
            String hospitalId, String wardId, PatientStatus status);

    List<Patient> findAllByHospitalIdAndWardIdAndStatusOrderByAdmissionDateDesc(
            String hospitalId, String wardId, PatientStatus status);

    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.wardId = :wardId " +
           "AND p.status = :status " +
           "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "  OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "ORDER BY p.admissionDate DESC")
    List<Patient> searchAdmittedByWardAndName(
            @Param("hospitalId") String hospitalId,
            @Param("wardId") String wardId,
            @Param("status") PatientStatus status,
            @Param("name") String name);

    List<Patient> findAllByHospitalIdAndStatusOrderByAdmissionDateAsc(
            String hospitalId, PatientStatus status);

    List<Patient> findAllByHospitalIdAndStatusOrderByAdmissionDateDesc(
            String hospitalId, PatientStatus status);

    List<Patient> findAllByHospitalIdOrderByAdmissionDateDesc(String hospitalId);

    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "  OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "ORDER BY p.admissionDate DESC")
    List<Patient> searchByHospitalAndName(
            @Param("hospitalId") String hospitalId,
            @Param("status") PatientStatus status,
            @Param("name") String name);

    List<Patient> findAllByHospitalIdAndWardIdAndAcuityColor(
            String hospitalId, String wardId, AcuityColor acuityColor);

    List<Patient> findAllByHospitalIdOrderByAdmissionDateAsc(String hospitalId);

    long countByHospitalIdAndStatus(String hospitalId, PatientStatus status);

    List<Patient> findAllByHospitalIdAndWardIdOrderByAdmissionDateDesc(String hospitalId, String wardId);
}
