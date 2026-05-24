package com.careround.patient.vitals;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.VhiStatus;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientVitalsRepository;
import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientVitalsServiceTest {

    @Mock private PatientVitalsRepository patientVitalsRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private AcuityComputationService acuityComputationService;
    @Mock private OutboxService outboxService;

    @InjectMocks private PatientVitalsServiceImpl patientVitalsService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID   = "patient-1";

    private Patient patient;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-nurse", UserRole.NURSE);
        patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
        patient.setAcuityColor(AcuityColor.GREEN);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void recordVitals_happyPath_computesVhiAndUpdatesPatient() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        when(acuityComputationService.computeScore(any(), any(), any(), any(), any())).thenReturn(0);
        when(acuityComputationService.computeVhiStatus(0)).thenReturn(VhiStatus.STABLE);
        when(acuityComputationService.toAcuityColor(VhiStatus.STABLE)).thenReturn(AcuityColor.GREEN);
        when(patientVitalsRepository.save(any())).thenAnswer(inv -> {
            PatientVitals v = inv.getArgument(0);
            v.setId("vitals-1");
            return v;
        });

        VitalsResponse result = patientVitalsService.recordVitals(PATIENT_ID, sampleRequest());

        assertThat(result.vhiScore()).isEqualTo(0);
        assertThat(result.vhiStatus()).isEqualTo(VhiStatus.STABLE);
        assertThat(patient.getAcuityColor()).isEqualTo(AcuityColor.GREEN);
        verify(outboxService).publish(eq("vitals-recorded"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void recordVitals_abnormalVitals_marksCritical() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        when(acuityComputationService.computeScore(any(), any(), any(), any(), any())).thenReturn(12);
        when(acuityComputationService.computeVhiStatus(12)).thenReturn(VhiStatus.CRITICAL);
        when(acuityComputationService.toAcuityColor(VhiStatus.CRITICAL)).thenReturn(AcuityColor.RED);
        when(patientVitalsRepository.save(any())).thenAnswer(inv -> {
            PatientVitals v = inv.getArgument(0);
            v.setId("vitals-2");
            return v;
        });

        VitalsResponse result = patientVitalsService.recordVitals(PATIENT_ID,
                new RecordVitalsRequest(130, 80, null, 30, new BigDecimal("35.0"), new BigDecimal("85.0"), null));

        assertThat(result.vhiStatus()).isEqualTo(VhiStatus.CRITICAL);
        assertThat(patient.getAcuityColor()).isEqualTo(AcuityColor.RED);
    }

    @Test
    void recordVitals_patientNotFound_throwsNotFoundException() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientVitalsService.recordVitals(PATIENT_ID, sampleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getVitalsHistory_limitCappedAt50() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        List<PatientVitals> hundredVitals = IntStream.range(0, 100)
                .mapToObj(i -> buildVitals("v-" + i))
                .toList();
        when(patientVitalsRepository.findAllByPatientIdAndHospitalIdOrderByRecordedAtDesc(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(hundredVitals);

        List<VitalsResponse> result = patientVitalsService.getVitalsHistory(PATIENT_ID, 100);

        assertThat(result).hasSize(50);
    }

    private RecordVitalsRequest sampleRequest() {
        return new RecordVitalsRequest(75, 120, 80, 12, new BigDecimal("37.0"), new BigDecimal("98.0"), null);
    }

    private PatientVitals buildVitals(String id) {
        PatientVitals v = new PatientVitals();
        v.setId(id);
        v.setPatientId(PATIENT_ID);
        v.setHospitalId(HOSPITAL_ID);
        v.setRecordedAt(LocalDateTime.now());
        v.setPulse(75);
        v.setSystolicBp(120);
        v.setRespiratoryRate(12);
        v.setSpo2(new BigDecimal("98"));
        v.setTemperature(new BigDecimal("37.0"));
        v.setVhiScore(0);
        v.setVhiStatus(VhiStatus.STABLE);
        return v;
    }
}
