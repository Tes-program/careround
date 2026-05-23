package com.careround.auth.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void enumShouldHaveExpectedValues() {
        UserRole[] values = UserRole.values();
        assertThat(values).hasSize(5);
        assertThat(values).containsExactlyInAnyOrder(
                UserRole.ADMIN,
                UserRole.DOCTOR,
                UserRole.NURSE,
                UserRole.SUPERVISOR,
                UserRole.PLATFORM_ADMIN
        );
    }
}
