package com.careround.patient.medicationchart;

import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationchart.enums.MedicationChartStatus;
import com.careround.test.DataJpaH2Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaH2Test
class MedicationChartRepositoryTest {

    @Autowired
    private MedicationChartRepository repository;

    private MedicationChart buildChart(String patientId, String hospitalId,
                                       String prescriptionId, MedicationChartStatus status) {
        MedicationChart chart = new MedicationChart();
        chart.setPatientId(patientId);
        chart.setHospitalId(hospitalId);
        chart.setPrescriptionId(prescriptionId);
        chart.setStatus(status);
        return chart;
    }

    @Test
    void findByPrescriptionIdAndHospitalId_wrongHospital_returnsEmpty() {
        repository.save(buildChart("patient-1", "hospital-A", "prescription-1", MedicationChartStatus.ACTIVE));

        Optional<MedicationChart> result = repository.findByPrescriptionIdAndHospitalId("prescription-1", "hospital-B");

        assertThat(result).isEmpty();
    }

    @Test
    void findByPrescriptionIdAndHospitalId_correctHospital_returnsPresent() {
        repository.save(buildChart("patient-1", "hospital-A", "prescription-1", MedicationChartStatus.ACTIVE));

        Optional<MedicationChart> result = repository.findByPrescriptionIdAndHospitalId("prescription-1", "hospital-A");

        assertThat(result).isPresent();
    }

    @Test
    void findByIdAndHospitalId_wrongHospital_returnsEmpty() {
        MedicationChart saved = repository.save(buildChart("patient-1", "hospital-A", "p-1", MedicationChartStatus.ACTIVE));

        Optional<MedicationChart> result = repository.findByIdAndHospitalId(saved.getId(), "hospital-B");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByPatientIdAndHospitalIdAndStatus_filtersCorrectly() {
        repository.save(buildChart("patient-1", "hospital-A", "p-1", MedicationChartStatus.ACTIVE));
        repository.save(buildChart("patient-1", "hospital-A", "p-2", MedicationChartStatus.COMPLETED));
        repository.save(buildChart("patient-1", "hospital-A", "p-3", MedicationChartStatus.DISCONTINUED));

        List<MedicationChart> results = repository.findAllByPatientIdAndHospitalIdAndStatus(
                "patient-1", "hospital-A", MedicationChartStatus.ACTIVE);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(MedicationChartStatus.ACTIVE);
    }
}
