package com.careround.patient.vitals;

import com.careround.hospital.entity.SystemConfiguration;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.ConsciousnessLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AcuityComputationService {

    public AcuityColor computeColor(int score, SystemConfiguration config) {
        if (score >= config.getAcuityRedThreshold()) return AcuityColor.RED;
        if (score >= config.getAcuityAmberThreshold()) return AcuityColor.AMBER;
        return AcuityColor.GREEN;
    }

    public int computeScore(Integer heartRate, Integer respiratoryRate,
                            BigDecimal oxygenSaturation, Integer systolicBP,
                            BigDecimal temperature, ConsciousnessLevel consciousnessLevel) {
        int score = 0;

        if (respiratoryRate != null) {
            int rr = respiratoryRate;
            if (rr <= 8) score += 3;
            else if (rr <= 11) score += 1;
            else if (rr <= 20) score += 0;
            else if (rr <= 24) score += 2;
            else score += 3;
        }

        if (oxygenSaturation != null) {
            int spo2 = oxygenSaturation.intValue();
            if (spo2 >= 96) score += 0;
            else if (spo2 >= 94) score += 1;
            else if (spo2 >= 92) score += 2;
            else score += 3;
        }

        if (systolicBP != null) {
            int sbp = systolicBP;
            if (sbp <= 90) score += 3;
            else if (sbp <= 100) score += 2;
            else if (sbp <= 110) score += 1;
            else if (sbp <= 219) score += 0;
            else score += 3;
        }

        if (heartRate != null) {
            int hr = heartRate;
            if (hr <= 40) score += 3;
            else if (hr <= 50) score += 1;
            else if (hr <= 90) score += 0;
            else if (hr <= 110) score += 1;
            else if (hr <= 130) score += 2;
            else score += 3;
        }

        if (temperature != null) {
            double temp = temperature.doubleValue();
            if (temp <= 35.0) score += 3;
            else if (temp <= 36.0) score += 1;
            else if (temp <= 38.0) score += 0;
            else if (temp <= 39.0) score += 1;
            else score += 2;
        }

        if (consciousnessLevel != null && consciousnessLevel != ConsciousnessLevel.ALERT) {
            score += 3;
        }

        return score;
    }
}
