package com.careround.audit.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AuditLogEntityTest {

    @Test
    void eventId_fieldExists() throws NoSuchFieldException {
        Field eventIdField = AuditLog.class.getDeclaredField("eventId");
        assertThat(eventIdField).isNotNull();
    }

    @Test
    void onCreateCallback_setsTimestampsAndId() {
        AuditLog log = new AuditLog();
        log.setEventType("careround.patient.admitted");
        log.setHospitalId("hosp-1");
        log.setPayload("{}");
        log.setReceivedAt(java.time.LocalDateTime.now());

        assertThatNoException().isThrownBy(() -> {
            java.lang.reflect.Method onCreate = AuditLog.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(log);
        });

        assertThat(log.getId()).isNotBlank();
        assertThat(log.getCreatedAt()).isNotNull();
        assertThat(log.getUpdatedAt()).isNotNull();
    }
}
