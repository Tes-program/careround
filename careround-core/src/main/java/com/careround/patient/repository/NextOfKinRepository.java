package com.careround.patient.repository;

import com.careround.patient.entity.NextOfKin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NextOfKinRepository extends JpaRepository<NextOfKin, String> {

    List<NextOfKin> findAllByPatientId(String patientId);

    Optional<NextOfKin> findByIdAndPatientId(String id, String patientId);

    @Query("SELECT n FROM NextOfKin n WHERE n.patientId = :patientId AND n.isEmergencyContact = true")
    List<NextOfKin> findEmergencyContactsByPatientId(@Param("patientId") String patientId);
}
