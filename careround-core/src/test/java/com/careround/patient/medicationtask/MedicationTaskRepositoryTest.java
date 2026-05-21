package com.careround.patient.medicationtask;

import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.test.DataJpaH2Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaH2Test
class MedicationTaskRepositoryTest {

    @Autowired
    private MedicationTaskRepository repository;

    private MedicationTask buildTask(String wardId, String hospitalId, String nurseId,
                                     MedicationTaskStatus status, LocalDateTime scheduledTime,
                                     LocalDateTime reminderSentAt) {
        MedicationTask task = new MedicationTask();
        task.setMedicationChartId("chart-1");
        task.setPatientId("patient-1");
        task.setHospitalId(hospitalId);
        task.setWardId(wardId);
        task.setAssignedNurseId(nurseId);
        task.setScheduledTime(scheduledTime);
        task.setStatus(status);
        task.setReminderSentAt(reminderSentAt);
        return task;
    }

    @Test
    void findAllByStatusAndScheduledTimeBefore_onlyReturnsPendingPastThreshold() {
        LocalDateTime threshold = LocalDateTime.now();
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, threshold.minusHours(2), null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, threshold.plusHours(1), null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.COMPLETED, threshold.minusHours(1), null));

        List<MedicationTask> results = repository.findAllByStatusAndScheduledTimeBefore(
                MedicationTaskStatus.PENDING, threshold);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(MedicationTaskStatus.PENDING);
    }

    @Test
    void findAllByWardIdAndHospitalIdAndStatusIn_scopedToWardAndHospital() {
        LocalDateTime now = LocalDateTime.now();
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.OVERDUE, now, null));
        repository.save(buildTask("ward-2", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));
        repository.save(buildTask("ward-1", "hospital-B", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));

        List<MedicationTask> results = repository.findAllByWardIdAndHospitalIdAndStatusIn(
                "ward-1", "hospital-A",
                List.of(MedicationTaskStatus.PENDING, MedicationTaskStatus.OVERDUE));

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(t -> t.getWardId().equals("ward-1")
                && t.getHospitalId().equals("hospital-A"));
    }

    @Test
    void findAllByAssignedNurseIdAndHospitalIdAndStatusIn_scopedToNurseAndHospital() {
        LocalDateTime now = LocalDateTime.now();
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-2",
                MedicationTaskStatus.PENDING, now, null));
        repository.save(buildTask("ward-1", "hospital-B", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));

        List<MedicationTask> results = repository.findAllByAssignedNurseIdAndHospitalIdAndStatusIn(
                "nurse-1", "hospital-A", List.of(MedicationTaskStatus.PENDING));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAssignedNurseId()).isEqualTo("nurse-1");
        assertThat(results.get(0).getHospitalId()).isEqualTo("hospital-A");
    }

    @Test
    void countByWardIdAndHospitalIdAndStatus_returnsCorrectCount() {
        LocalDateTime now = LocalDateTime.now();
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, now, null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.COMPLETED, now, null));

        long count = repository.countByWardIdAndHospitalIdAndStatus(
                "ward-1", "hospital-A", MedicationTaskStatus.PENDING);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull_excludesRemindedTasks() {
        LocalDateTime threshold = LocalDateTime.now();
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, threshold.minusHours(1), null));
        repository.save(buildTask("ward-1", "hospital-A", "nurse-1",
                MedicationTaskStatus.PENDING, threshold.minusHours(1), threshold.minusMinutes(10)));

        List<MedicationTask> results = repository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                MedicationTaskStatus.PENDING, threshold);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getReminderSentAt()).isNull();
    }
}
