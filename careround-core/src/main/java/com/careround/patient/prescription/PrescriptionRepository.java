package com.careround.patient.prescription;

import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.prescription.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    Optional<Prescription> findByIdAndHospitalId(String id, String hospitalId);

    List<Prescription> findAllByPatientIdAndHospitalId(String patientId, String hospitalId);

    List<Prescription> findAllByPatientIdAndHospitalIdAndStatus(
            String patientId, String hospitalId, PrescriptionStatus status);
}
