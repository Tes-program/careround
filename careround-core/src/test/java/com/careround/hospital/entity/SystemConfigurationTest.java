package com.careround.hospital.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemConfigurationTest {

    @Test
    void defaultValues_shouldMatchSpec() {
        SystemConfiguration config = new SystemConfiguration();
        assertThat(config.getAcuityAmberThreshold()).isEqualTo(5);
        assertThat(config.getAcuityRedThreshold()).isEqualTo(7);
        assertThat(config.getTaskOverdueReminderMinutes()).isEqualTo(10);
        assertThat(config.getTaskEscalationMinutes()).isEqualTo(20);
        assertThat(config.isPushNotificationsEnabled()).isTrue();
    }
}
