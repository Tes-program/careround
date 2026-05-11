package com.careround.patient.vitals;

import com.careround.auth.enums.UserRole;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.AcuityLevel;
import com.careround.patient.enums.ConsciousnessLevel;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientVitalsServiceTest {

    @Mock private PatientVitalsRepository patientVitalsRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private NewsScoreService newsScoreService;

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
        patient.setNewsScore(0);
        patient.setAcuityLevel(AcuityLevel.LOW);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void recordVitals_happyPath_computesNewsScoreAndUpdatesPatient() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        doAnswer(inv -> {
            Patient p = inv.getArgument(0);
            PatientVitals v = inv.getArgument(1);
            p.setNewsScore(3);
            p.setAcuityLevel(AcuityLevel.LOW);
            v.setNewsScore(3);
            return 3;
        }).when(newsScoreService).computeAndUpdate(any(), any());
        when(patientVitalsRepository.save(any())).thenAnswer(inv -> {
            PatientVitals v = inv.getArgument(0);
            v.setId("vitals-1");
            return v;
        });

        VitalsResponse result = patientVitalsService.recordVitals(PATIENT_ID, sampleRequest());

        assertThat(result.newsScore()).isEqualTo(3);
        assertThat(patient.getNewsScore()).isEqualTo(3);
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
        v.setRecordedAt(LocalDateTime.now());
        v.setHeartRate(75);
        v.setRespiratoryRate(16);
        v.setOxygenSaturation(new BigDecimal("98"));
        v.setSystolicBP(120);
        v.setTemperature(new BigDecimal("37.0"));
        v.setConsciousnessLevel(ConsciousnessLevel.ALERT);
        return v;
    }
}
