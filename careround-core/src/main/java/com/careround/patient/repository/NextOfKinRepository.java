package com.careround.patient.repository;

import com.careround.patient.entity.NextOfKin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NextOfKinRepository extends JpaRepository<NextOfKin, String> {

    List<NextOfKin> findAllByPatientId(String patientId);
}
