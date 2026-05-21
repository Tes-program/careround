package com.careround.patient.vitals;

import com.careround.auth.enums.UserRole;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.ConsciousnessLevel;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientVitalsRepository;
import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientVitalsServiceTest {

    @Mock private PatientVitalsRepository patientVitalsRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private ClinicalNoteRepository clinicalNoteRepository;

    @InjectMocks private PatientVitalsServiceImpl patientVitalsService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID = "patient-1";

    private Patient patient;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-nurse", UserRole.NURSE);

        patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void recordVitals_happyPath_computesNews2ScoreAndUpdatesPatient() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(patientVitalsRepository.save(any())).thenAnswer(inv -> {
            PatientVitals v = inv.getArgument(0);
            v.setId("vitals-1");
            return v;
        });

        // Normal vitals → all sub-scores 0 → computedScore=0, AcuityColor.GREEN
        VitalsResponse result = patientVitalsService.recordVitals(PATIENT_ID, sampleRequest());

        assertThat(result.computedScore()).isEqualTo(0);
        assertThat(result.acuityColor()).isEqualTo(AcuityColor.GREEN);
        assertThat(patient.getAcuityColor()).isEqualTo(AcuityColor.GREEN);
    }

    @Test
    void recordVitals_abnormalVitals_computesHighScore() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(patientVitalsRepository.save(any())).thenAnswer(inv -> {
            PatientVitals v = inv.getArgument(0);
            v.setId("vitals-2");
            return v;
        });

        // RR=30(+3), SpO2=85(+3), SBP=80(+3), HR=150(+3), Temp=35.0(+3), UNRESPONSIVE(+3) = 18
        RecordVitalsRequest criticalRequest = new RecordVitalsRequest(
                150, 30, new BigDecimal("85.0"),
                80, new BigDecimal("35.0"),
                ConsciousnessLevel.UNRESPONSIVE);

        VitalsResponse result = patientVitalsService.recordVitals(PATIENT_ID, criticalRequest);

        assertThat(result.computedScore()).isGreaterThanOrEqualTo(7);
        assertThat(result.acuityColor()).isEqualTo(AcuityColor.RED);
    }

    @Test
    void recordVitals_patientNotFound_throwsNotFoundException() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientVitalsService.recordVitals(PATIENT_ID, sampleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void recordVitals_crossTenantPatient_throwsAccessDeniedException() {
        Patient other = new Patient();
        other.setId(PATIENT_ID);
        other.setHospitalId("other-hosp");
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> patientVitalsService.recordVitals(PATIENT_ID, sampleRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getVitalsHistory_limitCappedAt50() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        List<PatientVitals> hundredVitals = IntStream.range(0, 100)
                .mapToObj(i -> buildVitals("v-" + i))
                .toList();
        when(patientVitalsRepository.findAllByPatientIdOrderByRecordedAtDesc(PATIENT_ID))
                .thenReturn(hundredVitals);

        List<VitalsResponse> result = patientVitalsService.getVitalsHistory(PATIENT_ID, 100);

        assertThat(result).hasSize(50);
    }

    private RecordVitalsRequest sampleRequest() {
        return new RecordVitalsRequest(
                75, 16, new BigDecimal("98.0"),
                120, new BigDecimal("37.0"),
                ConsciousnessLevel.ALERT);
    }

    private PatientVitals buildVitals(String id) {
        PatientVitals v = new PatientVitals();
        v.setId(id);
        v.setPatientId(PATIENT_ID);
        v.setHospitalId(HOSPITAL_ID);
        v.setRecordedAt(LocalDateTime.now());
        v.setHeartRate(75);
        v.setRespiratoryRate(16);
        v.setOxygenSaturation(new BigDecimal("98"));
        v.setSystolicBP(120);
        v.setTemperature(new BigDecimal("37.0"));
        v.setConsciousnessLevel(ConsciousnessLevel.ALERT);
        v.setComputedScore(0);
        v.setAcuityColor(AcuityColor.GREEN);
        return v;
    }
}
