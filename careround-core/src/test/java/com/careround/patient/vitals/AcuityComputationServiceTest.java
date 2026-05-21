package com.careround.patient.vitals;

import com.careround.hospital.entity.SystemConfiguration;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.ConsciousnessLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AcuityComputationServiceTest {

    private AcuityComputationService service;
    private SystemConfiguration config;

    @BeforeEach
    void setUp() {
        service = new AcuityComputationService();
        config = new SystemConfiguration();
        config.setAcuityAmberThreshold(5);
        config.setAcuityRedThreshold(7);
    }

    // ----- computeColor -----

    @Test
    void computeColor_returnsGreen_whenScoreBelowAmber() {
        assertThat(service.computeColor(4, config)).isEqualTo(AcuityColor.GREEN);
    }

    @Test
    void computeColor_returnsAmber_atAmberThreshold() {
        assertThat(service.computeColor(5, config)).isEqualTo(AcuityColor.AMBER);
    }

    @Test
    void computeColor_returnsAmber_betweenThresholds() {
        assertThat(service.computeColor(6, config)).isEqualTo(AcuityColor.AMBER);
    }

    @Test
    void computeColor_returnsRed_atRedThreshold() {
        assertThat(service.computeColor(7, config)).isEqualTo(AcuityColor.RED);
    }

    @Test
    void computeColor_returnsRed_aboveRedThreshold() {
        assertThat(service.computeColor(15, config)).isEqualTo(AcuityColor.RED);
    }

    // ----- computeScore — composite -----

    @Test
    void computeScore_returnsZero_whenAllVitalsNormal() {
        int score = service.computeScore(75, 16, new BigDecimal("98.0"), 120,
                new BigDecimal("37.0"), ConsciousnessLevel.ALERT);
        assertThat(score).isEqualTo(0);
    }

    @Test
    void computeScore_allCritical_returnsHighScore() {
        // RR=30(+3), SpO2=85(+3), SBP=80(+3), HR=150(+3), Temp=35.0(+3), UNRESPONSIVE(+3) = 18
        int score = service.computeScore(150, 30, new BigDecimal("85.0"), 80,
                new BigDecimal("35.0"), ConsciousnessLevel.UNRESPONSIVE);
        assertThat(score).isEqualTo(18);
    }

    @Test
    void computeScore_handlesNullVitals_treatsAsZeroContribution() {
        int score = service.computeScore(null, null, null, null, null, null);
        assertThat(score).isEqualTo(0);
    }

    // ----- Respiratory Rate bands -----

    @ParameterizedTest
    @CsvSource({
        "8,  3",
        "9,  1",
        "11, 1",
        "12, 0",
        "20, 0",
        "21, 2",
        "24, 2",
        "25, 3"
    })
    void computeScore_respiratoryRate_bands(int rr, int expectedContribution) {
        int score = service.computeScore(75, rr, new BigDecimal("98.0"), 120,
                new BigDecimal("37.0"), ConsciousnessLevel.ALERT);
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
        "99, 0"
    })
    void computeScore_oxygenSaturation_bands(int spo2, int expectedContribution) {
        int score = service.computeScore(75, 16, new BigDecimal(spo2), 120,
                new BigDecimal("37.0"), ConsciousnessLevel.ALERT);
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Systolic BP bands -----

    @ParameterizedTest
    @CsvSource({
        "90,  3",
        "91,  2",
        "100, 2",
        "101, 1",
        "110, 1",
        "111, 0",
        "219, 0",
        "220, 3"
    })
    void computeScore_systolicBP_bands(int sbp, int expectedContribution) {
        int score = service.computeScore(75, 16, new BigDecimal("98.0"), sbp,
                new BigDecimal("37.0"), ConsciousnessLevel.ALERT);
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Heart Rate bands -----

    @ParameterizedTest
    @CsvSource({
        "40,  3",
        "41,  1",
        "50,  1",
        "51,  0",
        "90,  0",
        "91,  1",
        "110, 1",
        "111, 2",
        "130, 2",
        "131, 3"
    })
    void computeScore_heartRate_bands(int hr, int expectedContribution) {
        int score = service.computeScore(hr, 16, new BigDecimal("98.0"), 120,
                new BigDecimal("37.0"), ConsciousnessLevel.ALERT);
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Temperature bands -----

    @ParameterizedTest
    @CsvSource({
        "35.0, 3",
        "35.1, 1",
        "36.0, 1",
        "36.1, 0",
        "38.0, 0",
        "38.1, 1",
        "39.0, 1",
        "39.1, 2"
    })
    void computeScore_temperature_bands(String temp, int expectedContribution) {
        int score = service.computeScore(75, 16, new BigDecimal("98.0"), 120,
                new BigDecimal(temp), ConsciousnessLevel.ALERT);
        assertThat(score).isEqualTo(expectedContribution);
    }

    // ----- Consciousness -----

    @Test
    void computeScore_alertConsciousness_addsZero() {
        int score = service.computeScore(75, 16, new BigDecimal("98.0"), 120,
                new BigDecimal("37.0"), ConsciousnessLevel.ALERT);
        assertThat(score).isEqualTo(0);
    }

    @Test
    void computeScore_nonAlertConsciousness_addsThree() {
        int score = service.computeScore(75, 16, new BigDecimal("98.0"), 120,
                new BigDecimal("37.0"), ConsciousnessLevel.UNRESPONSIVE);
        assertThat(score).isEqualTo(3);
    }
}
