package com.careround.patient.prescription;

import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.prescription.enums.PrescriptionStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrescriptionEntityTest {

    @Test
    void status_defaultsToActive() {
        Prescription prescription = new Prescription();
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.ACTIVE);
    }

    @Test
    void administrationTimes_defaultsToEmptyListNotNull() {
        Prescription prescription = new Prescription();
        assertThat(prescription.getAdministrationTimes()).isNotNull();
        assertThat(prescription.getAdministrationTimes()).isEmpty();
    }
}
