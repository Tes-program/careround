package com.careround.patient.medicationtask;

import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedicationTaskEntityTest {

    @Test
    void status_defaultsToPending() {
        MedicationTask task = new MedicationTask();
        assertThat(task.getStatus()).isEqualTo(MedicationTaskStatus.PENDING);
    }

    @Test
    void reminderSentAt_defaultsToNull() {
        MedicationTask task = new MedicationTask();
        assertThat(task.getReminderSentAt()).isNull();
    }

    @Test
    void completedAt_defaultsToNull() {
        MedicationTask task = new MedicationTask();
        assertThat(task.getCompletedAt()).isNull();
    }
}
