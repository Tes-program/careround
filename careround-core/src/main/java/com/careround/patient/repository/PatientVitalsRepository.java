package com.careround.patient.repository;

import com.careround.patient.entity.PatientVitals;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientVitalsRepository extends JpaRepository<PatientVitals, String> {

    List<PatientVitals> findAllByPatientIdOrderByRecordedAtDesc(String patientId);

    Optional<PatientVitals> findFirstByPatientIdOrderByRecordedAtDesc(String patientId);
}
