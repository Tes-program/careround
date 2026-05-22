package com.careround.patient.vitals;

import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.VhiStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AcuityComputationServiceTest {

    private AcuityComputationService service;

    @BeforeEach
    void setUp() {
        service = new AcuityComputationService();
    }

    // ----- computeVhiStatus -----

    @Test
    void computeVhiStatus_score0_returnsStable() {
        assertThat(service.computeVhiStatus(0)).isEqualTo(VhiStatus.STABLE);
    }

    @Test
    void computeVhiStatus_score2_returnsStable() {
        assertThat(service.computeVhiStatus(2)).isEqualTo(VhiStatus.STABLE);
    }

    @Test
    void computeVhiStatus_score3_returnsWatch() {
        assertThat(service.computeVhiStatus(3)).isEqualTo(VhiStatus.WATCH);
    }

    @Test
    void computeVhiStatus_score4_returnsWatch() {
        assertThat(service.computeVhiStatus(4)).isEqualTo(VhiStatus.WATCH);
    }

    @Test
    void computeVhiStatus_score5_returnsCritical() {
        assertThat(service.computeVhiStatus(5)).isEqualTo(VhiStatus.CRITICAL);
    }

    @Test
    void computeVhiStatus_score15_returnsCritical() {
        assertThat(service.computeVhiStatus(15)).isEqualTo(VhiStatus.CRITICAL);
    }

    // ----- toAcuityColor -----

    @Test
    void toAcuityColor_stable_returnsGreen() {
        assertThat(service.toAcuityColor(VhiStatus.STABLE)).isEqualTo(AcuityColor.GREEN);
    }

    @Test
    void toAcuityColor_watch_returnsAmber() {
        assertThat(service.toAcuityColor(VhiStatus.WATCH)).isEqualTo(AcuityColor.AMBER);
    }

    @Test
    void toAcuityColor_critical_returnsRed() {
        assertThat(service.toAcuityColor(VhiStatus.CRITICAL)).isEqualTo(AcuityColor.RED);
    }

    // ----- computeScore — all normal returns 0 -----

    @Test
    void computeScore_returnsZero_whenAllVitalsNormal() {
        // pulse=75, sbp=120, rr=12, temp=37.0, spo2=98
        int score = service.computeScore(75, 120, 12, new BigDecimal("37.0"), new BigDecimal("98.0"));
        assertThat(score).isEqualTo(0);
    }

    @Test
    void computeScore_allNull_returnsZero() {
        assertThat(service.computeScore(null, null, null, null, null)).isEqualTo(0);
    }

    // ----- Pulse bands -----

    @ParameterizedTest
    @CsvSource({
        "40,  3",
        "41,  2",
        "50,  2",
        "51,  1",
        "60,  1",
        "61,  0",
        "100, 0",
        "101, 1",
        "110, 1",
        "111, 2",
        "129, 2",
        "130, 3"
    })
    void computeScore_pulse_bands(int pulse, int expectedContribution) {
        int score = service.computeScore(pulse, 120, 12, new BigDecimal("37.0"), new BigDecimal("98.0"));
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Systolic BP bands -----

    @ParameterizedTest
    @CsvSource({
        "80,  3",
        "81,  2",
        "90,  2",
        "91,  1",
        "100, 1",
        "101, 0",
        "159, 0",
        "160, 1",
        "199, 1",
        "200, 2"
    })
    void computeScore_systolicBp_bands(int sbp, int expectedContribution) {
        int score = service.computeScore(75, sbp, 12, new BigDecimal("37.0"), new BigDecimal("98.0"));
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Respiratory Rate bands -----

    @ParameterizedTest
    @CsvSource({
        "8,  3",
        "9,  0",
        "14, 0",
        "15, 1",
        "20, 1",
        "21, 2",
        "29, 2",
        "30, 3"
    })
    void computeScore_respiratoryRate_bands(int rr, int expectedContribution) {
        int score = service.computeScore(75, 120, rr, new BigDecimal("37.0"), new BigDecimal("98.0"));
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Temperature bands -----

    @ParameterizedTest
    @CsvSource({
        "35.0, 3",
        "35.1, 2",
        "36.0, 2",
        "36.1, 0",
        "37.4, 0",
        "37.5, 1",
        "38.4, 1",
        "38.5, 2",
        "38.9, 2",
        "39.0, 3"
    })
    void computeScore_temperature_bands(String temp, int expectedContribution) {
        int score = service.computeScore(75, 120, 12, new BigDecimal(temp), new BigDecimal("98.0"));
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- SpO2 bands -----

    @ParameterizedTest
    @CsvSource({
        "91, 3",
        "92, 2",
        "93, 2",
        "94, 1",
        "95, 1",
        "96, 0",
        "100, 0"
    })
    void computeScore_spo2_bands(int spo2, int expectedContribution) {
        int score = service.computeScore(75, 120, 12, new BigDecimal("37.0"), new BigDecimal(spo2));
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- max score with all-critical inputs -----

    @Test
    void computeScore_allCritical_returnsMaxScore() {
        // pulse=130(+3), sbp=80(+3), rr=30(+3), temp=35.0(+3), spo2=91(+3) = 15
        int score = service.computeScore(130, 80, 30, new BigDecimal("35.0"), new BigDecimal("91.0"));
        assertThat(score).isEqualTo(15);
    }
}
