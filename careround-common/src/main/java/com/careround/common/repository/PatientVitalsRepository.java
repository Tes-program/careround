package com.careround.common.repository;

import com.careround.common.entity.PatientVitals;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientVitalsRepository extends JpaRepository<PatientVitals, String> {
    List<PatientVitals> findByPatientIdOrderByRecordedAtDesc(String patientId);
    Optional<PatientVitals> findFirstByPatientIdOrderByRecordedAtDesc(String patientId);
}
