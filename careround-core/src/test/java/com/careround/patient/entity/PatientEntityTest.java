package com.careround.patient.entity;

import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.PatientStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatientEntityTest {

    @Test
    void defaultAcuityColor_shouldBeGreen() {
        Patient patient = new Patient();
        assertThat(patient.getAcuityColor()).isEqualTo(AcuityColor.GREEN);
    }

    @Test
    void defaultStatus_shouldBeAdmitted() {
        Patient patient = new Patient();
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.ADMITTED);
    }
}
