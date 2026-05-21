package com.careround.patient.repository;

import com.careround.patient.entity.Patient;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    Optional<Patient> findByIdAndHospitalId(String id, String hospitalId);

    Optional<Patient> findByHospitalNumber(String hospitalNumber);

    List<Patient> findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(
            String hospitalId, String wardId);

    List<Patient> findAllByHospitalIdAndWardIdAndStatusOrderByAdmissionDateAsc(
            String hospitalId, String wardId, PatientStatus status);

    List<Patient> findAllByHospitalIdAndStatusOrderByAdmissionDateAsc(
            String hospitalId, PatientStatus status);

    List<Patient> findAllByHospitalIdAndWardIdAndAcuityColor(
            String hospitalId, String wardId, AcuityColor acuityColor);

    long countByHospitalIdAndStatus(String hospitalId, PatientStatus status);
}
