package com.careround.patient.clinicalnote;

import com.careround.auth.enums.UserRole;
import com.careround.patient.clinicalnote.dto.ConfirmNoteRequest;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.NoteType;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.dto.CreatePrescriptionRequest;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import com.careround.test.DataJpaH2Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@DataJpaH2Test
@Import(ClinicalNoteServiceImpl.class)
class ClinicalNoteConfirmRollbackTest {

    @Autowired private ClinicalNoteServiceImpl clinicalNoteService;
    @Autowired private ClinicalNoteRepository clinicalNoteRepository;
    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private PatientRepository patientRepository;

    @MockitoBean private OutboxService outboxService;

    private static final String HOSPITAL_ID = "hosp-rollback";
    private static final String PATIENT_ID = "patient-rollback";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-doctor", UserRole.DOCTOR);

        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
        patient.setFirstName("Jane");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1980, 1, 1));
        patient.setGender("F");
        patient.setHospitalNumber("HN-ROLLBACK");
        patient.setStatus(PatientStatus.ADMITTED);
        patient.setAdmissionDate(LocalDateTime.now());
        patientRepository.save(patient);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void confirm_rollsBackNoteAndPrescriptions_whenOutboxThrows() {
        doThrow(new RuntimeException("Outbox failure")).when(outboxService).publish(any(), any(), any());

        ConfirmNoteRequest request = new ConfirmNoteRequest(
                PATIENT_ID, NoteType.WARD_ROUND_NOTE,
                "Ward round note", null, false, null,
                List.of(prescriptionReq("Aspirin")));

        assertThatThrownBy(() -> clinicalNoteService.confirm(request))
                .isInstanceOf(RuntimeException.class);

        assertThat(clinicalNoteRepository.count()).isZero();
        assertThat(prescriptionRepository.count()).isZero();
    }

    private CreatePrescriptionRequest prescriptionReq(String drug) {
        return new CreatePrescriptionRequest(
                drug, "100mg", "oral", "daily", 24, 7,
                LocalDateTime.now().plusHours(1),
                List.of(LocalDateTime.now().plusHours(1)));
    }
}
