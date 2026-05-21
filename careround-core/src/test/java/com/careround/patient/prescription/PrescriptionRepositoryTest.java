package com.careround.patient.prescription;

import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.prescription.enums.PrescriptionStatus;
import com.careround.test.DataJpaH2Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaH2Test
class PrescriptionRepositoryTest {

    @Autowired
    private PrescriptionRepository repository;

    private Prescription buildPrescription(String patientId, String hospitalId, PrescriptionStatus status) {
        Prescription p = new Prescription();
        p.setPatientId(patientId);
        p.setHospitalId(hospitalId);
        p.setDrugName("Amoxicillin");
        p.setDose("500mg");
        p.setRoute("oral");
        p.setFrequencyString("every 8 hours");
        p.setFrequencyHours(8);
        p.setTotalDoses(21);
        p.setStartTime(LocalDateTime.now());
        p.setAdministrationTimes(List.of(LocalDateTime.now()));
        p.setConfirmedById("doctor-1");
        p.setConfirmedAt(LocalDateTime.now());
        p.setStatus(status);
        return p;
    }

    @Test
    void findByIdAndHospitalId_wrongHospital_returnsEmpty() {
        Prescription saved = repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.ACTIVE));

        Optional<Prescription> result = repository.findByIdAndHospitalId(saved.getId(), "hospital-B");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndHospitalId_correctHospital_returnsPresent() {
        Prescription saved = repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.ACTIVE));

        Optional<Prescription> result = repository.findByIdAndHospitalId(saved.getId(), "hospital-A");

        assertThat(result).isPresent();
    }

    @Test
    void findAllByPatientIdAndHospitalId_scopedToPatientAndHospital() {
        repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.ACTIVE));
        repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.COMPLETED));
        repository.save(buildPrescription("patient-2", "hospital-A", PrescriptionStatus.ACTIVE));

        List<Prescription> results = repository.findAllByPatientIdAndHospitalId("patient-1", "hospital-A");

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(p -> p.getPatientId().equals("patient-1"));
    }

    @Test
    void findAllByPatientIdAndHospitalIdAndStatus_filtersCorrectly() {
        repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.ACTIVE));
        repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.DISCONTINUED));
        repository.save(buildPrescription("patient-1", "hospital-A", PrescriptionStatus.COMPLETED));

        List<Prescription> results = repository.findAllByPatientIdAndHospitalIdAndStatus(
                "patient-1", "hospital-A", PrescriptionStatus.ACTIVE);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(PrescriptionStatus.ACTIVE);
    }

    @Test
    void administrationTimes_survivesRoundTrip() {
        LocalDateTime t1 = LocalDateTime.of(2025, 6, 1, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 6, 1, 16, 0);
        Prescription p = buildPrescription("patient-1", "hospital-A", PrescriptionStatus.ACTIVE);
        p.setAdministrationTimes(List.of(t1, t2));

        Prescription saved = repository.save(p);
        repository.flush();
        Prescription reloaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getAdministrationTimes()).containsExactly(t1, t2);
    }
}
