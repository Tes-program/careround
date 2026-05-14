package com.careround.patient.vitals;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.hospital.SystemConfigurationService;
import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.AcuityLevel;
import com.careround.patient.enums.ConsciousnessLevel;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.escalation.EscalationService;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsScoreServiceTest {

    @Mock private SystemConfigurationService systemConfigurationService;
    @Mock private EscalationService escalationService;
    @Spy private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks private NewsScoreService newsScoreService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID = "patient-1";

    private SystemConfigResponse config;
    private Patient patient;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-1", UserRole.NURSE);
        config = new SystemConfigResponse("cfg-1", HOSPITAL_ID, 5, 7, 30, true, true);
        patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void compute_allNormalVitals_returnsZero() {
        PatientVitals vitals = normalVitals();

        int score = newsScoreService.compute(vitals);

        assertThat(score).isEqualTo(0);
    }

    @Test
    void compute_criticalRespiratoryRate_returnsCorrectScore() {
        PatientVitals vitals = normalVitals();
        vitals.setRespiratoryRate(26); // >= 25 → 3 points

        int score = newsScoreService.compute(vitals);

        assertThat(score).isEqualTo(3);
    }

    @Test
    void compute_unconsciousPatient_addsThreePoints() {
        PatientVitals vitals = normalVitals();
        vitals.setConsciousnessLevel(ConsciousnessLevel.UNRESPONSIVE); // → 3 points

        int score = newsScoreService.compute(vitals);

        assertThat(score).isEqualTo(3);
    }

    @Test
    void compute_multipleAbnormalVitals_sumIsCorrect() {
        PatientVitals vitals = normalVitals();
        vitals.setRespiratoryRate(7);           // <= 8 → 3
        vitals.setHeartRate(35);                // <= 40 → 3
        vitals.setConsciousnessLevel(ConsciousnessLevel.VOICE); // → 3

        int score = newsScoreService.compute(vitals);

        assertThat(score).isEqualTo(9);
    }

    @Test
    void computeAndUpdate_scoreAboveRedThreshold_triggersRedEscalation() {
        when(systemConfigurationService.getByHospitalId(HOSPITAL_ID)).thenReturn(config);
        EscalationResponse mockEscalation = mockEscalationResponse(EscalationSeverity.RED);
        when(escalationService.triggerSystemEscalation(eq(PATIENT_ID), eq(EscalationSeverity.RED), any()))
                .thenReturn(mockEscalation);

        // Build vitals that give score >= 7
        PatientVitals vitals = normalVitals();
        vitals.setRespiratoryRate(7);           // 3
        vitals.setHeartRate(35);                // 3
        vitals.setConsciousnessLevel(ConsciousnessLevel.VOICE); // 3 → total = 9

        int score = newsScoreService.computeAndUpdate(patient, vitals);

        assertThat(score).isGreaterThanOrEqualTo(7);
        assertThat(patient.getAcuityLevel()).isEqualTo(AcuityLevel.HIGH);
        verify(escalationService).triggerSystemEscalation(eq(PATIENT_ID), eq(EscalationSeverity.RED), any());
    }

    @Test
    void computeAndUpdate_scoreBelowThreshold_noEscalationTriggered() {
        when(systemConfigurationService.getByHospitalId(HOSPITAL_ID)).thenReturn(config);

        PatientVitals vitals = normalVitals(); // score = 0

        newsScoreService.computeAndUpdate(patient, vitals);

        verify(escalationService, never()).triggerSystemEscalation(any(), any(), any());
    }

    @Test
    void computeAndUpdate_openEscalationAlreadyExists_noDuplicate() {
        when(systemConfigurationService.getByHospitalId(HOSPITAL_ID)).thenReturn(config);
        // triggerSystemEscalation returns existing (de-dup is handled inside EscalationService)
        EscalationResponse existing = mockEscalationResponse(EscalationSeverity.RED);
        when(escalationService.triggerSystemEscalation(eq(PATIENT_ID), eq(EscalationSeverity.RED), any()))
                .thenReturn(existing);

        PatientVitals vitals = normalVitals();
        vitals.setRespiratoryRate(7);
        vitals.setHeartRate(35);
        vitals.setConsciousnessLevel(ConsciousnessLevel.PAIN);

        newsScoreService.computeAndUpdate(patient, vitals);

        // Only called once — EscalationService internally deduplicates
        verify(escalationService).triggerSystemEscalation(eq(PATIENT_ID), eq(EscalationSeverity.RED), any());
    }

    private PatientVitals normalVitals() {
        PatientVitals v = new PatientVitals();
        v.setRespiratoryRate(16);
        v.setOxygenSaturation(new BigDecimal("98"));
        v.setSystolicBP(120);
        v.setHeartRate(75);
        v.setTemperature(new BigDecimal("37.0"));
        v.setConsciousnessLevel(ConsciousnessLevel.ALERT);
        v.setRecordedAt(LocalDateTime.now());
        return v;
    }

    private EscalationResponse mockEscalationResponse(EscalationSeverity severity) {
        return new EscalationResponse("esc-1", PATIENT_ID, HOSPITAL_ID, null, null,
                null, severity, null, null, null, LocalDateTime.now(), LocalDateTime.now());
    }
}
