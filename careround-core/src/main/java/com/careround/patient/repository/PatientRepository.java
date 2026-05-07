package com.careround.patient.repository;

import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    List<Patient> findAllByHospitalIdAndWardIdOrderByAcuityLevelDescNewsScoreDesc(
            String hospitalId, String wardId);

    Optional<Patient> findByIdAndHospitalId(String id, String hospitalId);

    Optional<Patient> findByHospitalNumber(String hospitalNumber);

    List<Patient> findAllByMedicalTeamIdAndStatus(String teamId, PatientStatus status);
}
