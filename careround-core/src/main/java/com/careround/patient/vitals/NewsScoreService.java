package com.careround.patient.vitals;

import com.careround.hospital.hospital.SystemConfigurationService;
import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.AcuityLevel;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationTrigger;
import com.careround.patient.escalation.EscalationService;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsScoreService {

    private final SystemConfigurationService systemConfigurationService;
    private final EscalationService escalationService;

    public int computeAndUpdate(Patient patient, PatientVitals vitals) {
        int score = compute(vitals);

        vitals.setNewsScore(score);
        patient.setNewsScore(score);
        patient.setAcuityLevel(deriveAcuityLevel(score));

        String hospitalId = HospitalContextHolder.getHospitalId();
        SystemConfigResponse config = systemConfigurationService.getByHospitalId(hospitalId);

        if (score >= config.newsRedThreshold()) {
            triggerIfNoOpenEscalation(patient.getId(), EscalationSeverity.RED, score);
        } else if (score >= config.newsAmberThreshold()) {
            triggerIfNoOpenEscalation(patient.getId(), EscalationSeverity.AMBER, score);
        }

        log.info("action=computeNewsScore patientId={} hospitalId={} score={} acuity={}",
                patient.getId(), hospitalId, score, patient.getAcuityLevel());
        return score;
    }

    public int compute(PatientVitals vitals) {
        int score = 0;
        score += respiratoryRateScore(vitals.getRespiratoryRate());
        score += oxygenSaturationScore(vitals.getOxygenSaturation());
        score += systolicBpScore(vitals.getSystolicBP());
        score += heartRateScore(vitals.getHeartRate());
        score += temperatureScore(vitals.getTemperature());
        score += consciousnessScore(vitals);
        return score;
    }

    private void triggerIfNoOpenEscalation(String patientId, EscalationSeverity severity, int score) {
        try {
            EscalationResponse result = escalationService.triggerSystemEscalation(
                    patientId, severity, "NEWS2 score " + score + " breached threshold");
            log.info("action=escalationTriggered patientId={} severity={} escalationId={}",
                    patientId, severity, result.id());
        } catch (Exception e) {
            log.error("action=escalationTriggerFailed patientId={} severity={} error={}",
                    patientId, severity, e.getMessage(), e);
        }
    }

    private AcuityLevel deriveAcuityLevel(int score) {
        if (score <= 4) return AcuityLevel.LOW;
        if (score <= 6) return AcuityLevel.MEDIUM;
        return AcuityLevel.HIGH;
    }

    private int respiratoryRateScore(int rr) {
        if (rr <= 8) return 3;
        if (rr <= 11) return 1;
        if (rr <= 20) return 0;
        if (rr <= 24) return 2;
        return 3;
    }

    private int oxygenSaturationScore(BigDecimal spo2) {
        int val = spo2.intValue();
        if (val <= 91) return 3;
        if (val <= 93) return 2;
        if (val <= 95) return 1;
        return 0;
    }

    private int systolicBpScore(int sbp) {
        if (sbp <= 90) return 3;
        if (sbp <= 100) return 2;
        if (sbp <= 110) return 1;
        if (sbp <= 219) return 0;
        return 3;
    }

    private int heartRateScore(int hr) {
        if (hr <= 40) return 3;
        if (hr <= 50) return 1;
        if (hr <= 90) return 0;
        if (hr <= 110) return 1;
        if (hr <= 130) return 2;
        return 3;
    }

    private int temperatureScore(BigDecimal temp) {
        if (temp.compareTo(new BigDecimal("35.0")) <= 0) return 3;
        if (temp.compareTo(new BigDecimal("36.0")) <= 0) return 1;
        if (temp.compareTo(new BigDecimal("38.0")) <= 0) return 0;
        if (temp.compareTo(new BigDecimal("39.0")) <= 0) return 1;
        return 2;
    }

    private int consciousnessScore(PatientVitals vitals) {
        return switch (vitals.getConsciousnessLevel()) {
            case ALERT -> 0;
            case VOICE, PAIN, UNRESPONSIVE -> 3;
        };
    }
}
