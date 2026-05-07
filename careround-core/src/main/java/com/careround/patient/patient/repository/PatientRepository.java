package com.careround.patient.patient.repository;

import com.careround.patient.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    List<Patient> findByHospitalIdAndWardIdOrderByNewsScoreDesc(String hospitalId, String wardId);
    List<Patient> findByHospitalIdAndHospitalNumber(String hospitalId, String hospitalNumber);
}
