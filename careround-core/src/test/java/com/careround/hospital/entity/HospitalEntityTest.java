package com.careround.hospital.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HospitalEntityTest {

    @Test
    void defaultValues_isActiveShouldBeTrue() {
        Hospital hospital = new Hospital();
        assertThat(hospital.isActive()).isTrue();
    }

    @Test
    void setCode_shouldPersistValue() {
        Hospital hospital = new Hospital();
        hospital.setCode("CITY001");
        assertThat(hospital.getCode()).isEqualTo("CITY001");
    }
}
