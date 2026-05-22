package com.careround.patient.vitals;

import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.VhiStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Computes the Vitals Health Index (VHI) score from five inputs using fixed clinical thresholds.
 * Thresholds are not configurable per hospital — they follow the VHI specification exactly.
 *
 * Score 0-2  → STABLE  → GREEN
 * Score 3-4  → WATCH   → AMBER
 * Score 5+   → CRITICAL → RED
 */
@Service
public class AcuityComputationService {

    public VhiStatus computeVhiStatus(int vhiScore) {
        if (vhiScore >= 5) return VhiStatus.CRITICAL;
        if (vhiScore >= 3) return VhiStatus.WATCH;
        return VhiStatus.STABLE;
    }

    public AcuityColor toAcuityColor(VhiStatus vhiStatus) {
        return switch (vhiStatus) {
            case CRITICAL -> AcuityColor.RED;
            case WATCH    -> AcuityColor.AMBER;
            case STABLE   -> AcuityColor.GREEN;
        };
    }

    public int computeScore(Integer pulse, Integer systolicBp,
                            Integer respiratoryRate, BigDecimal temperature, BigDecimal spo2) {
        int score = 0;

        if (pulse != null) {
            int hr = pulse;
            if (hr <= 40 || hr >= 130)         score += 3;
            else if (hr <= 50 || hr >= 111)     score += 2;
            else if (hr <= 60 || hr >= 101)     score += 1;
            // 61-100 → 0
        }

        if (systolicBp != null) {
            int sbp = systolicBp;
            if (sbp <= 80)                      score += 3;
            else if (sbp <= 90 || sbp >= 200)   score += 2;
            else if (sbp <= 100 || sbp >= 160)  score += 1;
            // 101-159 → 0
        }

        if (respiratoryRate != null) {
            int rr = respiratoryRate;
            if (rr <= 8 || rr >= 30)            score += 3;
            else if (rr >= 21)                  score += 2;
            else if (rr >= 15)                  score += 1;
            // 9-14 → 0
        }

        if (temperature != null) {
            double temp = temperature.doubleValue();
            if (temp <= 35.0 || temp >= 39.0)   score += 3;
            else if (temp <= 36.0 || temp >= 38.5) score += 2;
            else if (temp <= 37.4)              /* 36.1-37.4 → 0 */ ;
            else if (temp <= 38.4)              score += 1;
            // 37.5-38.4 → 1, 38.5-38.9 → 2, ≥39.0 → 3
        }

        if (spo2 != null) {
            int o2 = spo2.intValue();
            if (o2 <= 91)       score += 3;
            else if (o2 <= 93)  score += 2;
            else if (o2 <= 95)  score += 1;
            // 96-100 → 0
        }

        return score;
    }
}
