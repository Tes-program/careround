package com.careround.patient.vitals.repository;

import com.careround.patient.vitals.entity.PatientVitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientVitalsRepository extends JpaRepository<PatientVitals, String> {
}

