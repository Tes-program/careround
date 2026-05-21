package com.careround.hospital.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WardEntityTest {

    @Test
    void defaultIsActive_shouldBeTrue() {
        Ward ward = new Ward();
        assertThat(ward.isActive()).isTrue();
    }

    @Test
    void noSupervisorIdField() {
        boolean hasSupervisorId = false;
        for (var field : Ward.class.getDeclaredFields()) {
            if (field.getName().equals("supervisorId")) {
                hasSupervisorId = true;
                break;
            }
        }
        assertThat(hasSupervisorId).isFalse();
    }
}
