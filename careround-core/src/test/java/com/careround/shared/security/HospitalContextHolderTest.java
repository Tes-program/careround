package com.careround.shared.security;

import com.careround.auth.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


class HospitalContextHolderTest {

    @AfterEach
    void cleanup() {
        HospitalContextHolder.clear();
    }

    @Test
    void set_andGet_shouldReturnCorrectValues() {
        HospitalContextHolder.set("hospital-1", "user-1", UserRole.DOCTOR);

        assertThat(HospitalContextHolder.getHospitalId()).isEqualTo("hospital-1");
        assertThat(HospitalContextHolder.getUserId()).isEqualTo("user-1");
        assertThat(HospitalContextHolder.getRole()).isEqualTo(UserRole.DOCTOR);
    }

    @Test
    void clear_shouldRemoveContext() {
        HospitalContextHolder.set("hospital-1", "user-1", UserRole.NURSE);
        HospitalContextHolder.clear();

        assertThatThrownBy(HospitalContextHolder::getHospitalId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No hospital context");
    }

    @Test
    void hasRole_withMatchingRole_shouldReturnTrue() {
        HospitalContextHolder.set("h", "u", UserRole.SUPERVISOR);

        assertThat(HospitalContextHolder.hasRole(UserRole.SUPERVISOR)).isTrue();
        assertThat(HospitalContextHolder.hasRole(UserRole.DOCTOR)).isFalse();
    }

    @Test
    void hasAnyRole_withOneMatch_shouldReturnTrue() {
        HospitalContextHolder.set("h", "u", UserRole.NURSE);

        assertThat(HospitalContextHolder.hasAnyRole(UserRole.DOCTOR, UserRole.NURSE)).isTrue();
        assertThat(HospitalContextHolder.hasAnyRole(UserRole.ADMIN, UserRole.SUPERVISOR)).isFalse();
    }

    @Test
    void getHospitalId_withNoContextSet_shouldThrowIllegalStateException() {
        assertThatThrownBy(HospitalContextHolder::getHospitalId)
                .isInstanceOf(IllegalStateException.class);
    }

}
