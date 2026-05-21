package com.careround.patient.medicationchart;

import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationchart.enums.MedicationChartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationChartRepository extends JpaRepository<MedicationChart, String> {

    Optional<MedicationChart> findByIdAndHospitalId(String id, String hospitalId);

    Optional<MedicationChart> findByPrescriptionIdAndHospitalId(
            String prescriptionId, String hospitalId);

    List<MedicationChart> findAllByPatientIdAndHospitalId(
            String patientId, String hospitalId);

    List<MedicationChart> findAllByPatientIdAndHospitalIdAndStatus(
            String patientId, String hospitalId, MedicationChartStatus status);
}
