package com.careround.scheduler.jobs;

import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.scheduler.service.MedicationTaskOverdueProcessor;
import com.careround.shared.event.MedicationTaskOverdueEvent;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationTaskOverdueProcessorTest {

    @Mock private MedicationTaskRepository medicationTaskRepository;
    @Mock private MedicationChartRepository medicationChartRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private MedicationTaskOverdueProcessor processor;

    @Test
    void processOverdueTasks_marksOverdue_andSetsReminderSentAt() {
        MedicationTask task = overdueTask("task-1", "hosp-1", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        processor.processOverdueTasks();

        assertThat(task.getStatus()).isEqualTo(MedicationTaskStatus.OVERDUE);
        assertThat(task.getReminderSentAt()).isNotNull();
    }

    @Test
    void processOverdueTasks_returnsCountOfProcessed() {
        MedicationTask t1 = overdueTask("task-1", "hosp-1", "chart-1");
        MedicationTask t2 = overdueTask("task-2", "hosp-1", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(t1, t2)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        int count = processor.processOverdueTasks();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void processOverdueTasks_doesNothing_whenNoOverdueTasks() {
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        int count = processor.processOverdueTasks();

        assertThat(count).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void processOverdueTasks_computesMinutesOverdue_correctly() {
        MedicationTask task = overdueTask("task-1", "hosp-1", "chart-1");
        task.setScheduledTime(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(45));
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        ArgumentCaptor<MedicationTaskOverdueEvent> captor = ArgumentCaptor.forClass(MedicationTaskOverdueEvent.class);

        processor.processOverdueTasks();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().minutesOverdue()).isGreaterThanOrEqualTo(44);
    }

    @Test
    void processOverdueTasks_loadsChartAndPrescription_forEventPayload() {
        MedicationTask task = overdueTask("task-1", "hosp-1", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        processor.processOverdueTasks();

        verify(medicationChartRepository).findByIdAndHospitalId("chart-1", "hosp-1");
        verify(prescriptionRepository).findByIdAndHospitalId("rx-1", "hosp-1");
    }

    @Test
    void processOverdueTasks_publishesToCorrectTopic() {
        MedicationTask task = overdueTask("task-1", "hosp-1", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        processor.processOverdueTasks();

        verify(outboxService).publish(eq("medication-task-overdue"), any(), eq("hosp-1"));
    }

    @Test
    void processOverdueTasks_usesHospitalIdFromTask_notContextHolder() {
        MedicationTask task = overdueTask("task-1", "hosp-specific", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-specific");

        HospitalContextHolder.clear();
        processor.processOverdueTasks();

        verify(outboxService).publish(any(), any(), eq("hosp-specific"));
    }

    @Test
    void processOverdueTasks_handlesMultiplePages() {
        List<MedicationTask> page1 = buildTasks(200, "hosp-1", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(page1, Pageable.ofSize(200), 200))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        int count = processor.processOverdueTasks();

        assertThat(count).isEqualTo(200);
    }

    @Test
    void processOverdueTasks_includesDrugName_andDose_inEvent() {
        MedicationTask task = overdueTask("task-1", "hosp-1", "chart-1");
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)))
                .thenReturn(new PageImpl<>(List.of()));
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        ArgumentCaptor<MedicationTaskOverdueEvent> captor = ArgumentCaptor.forClass(MedicationTaskOverdueEvent.class);

        processor.processOverdueTasks();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().drugName()).isEqualTo("Aspirin");
        assertThat(captor.getValue().dose()).isEqualTo("100mg");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private MedicationTask overdueTask(String id, String hospitalId, String chartId) {
        MedicationTask t = new MedicationTask();
        t.setId(id);
        t.setPatientId("patient-1");
        t.setHospitalId(hospitalId);
        t.setWardId("ward-1");
        t.setMedicationChartId(chartId);
        t.setScheduledTime(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        t.setStatus(MedicationTaskStatus.PENDING);
        return t;
    }

    private List<MedicationTask> buildTasks(int count, String hospitalId, String chartId) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> overdueTask("task-" + i, hospitalId, chartId))
                .toList();
    }

    private void setupChartAndPrescription(String chartId, String prescriptionId, String hospitalId) {
        MedicationChart chart = new MedicationChart();
        chart.setId(chartId);
        chart.setHospitalId(hospitalId);
        chart.setPrescriptionId(prescriptionId);

        Prescription prescription = new Prescription();
        prescription.setId(prescriptionId);
        prescription.setHospitalId(hospitalId);
        prescription.setDrugName("Aspirin");
        prescription.setDose("100mg");
        prescription.setRoute("oral");
        prescription.setFrequencyString("daily");
        prescription.setFrequencyHours(24);
        prescription.setTotalDoses(7);
        prescription.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
        prescription.setConfirmedById("user-1");
        prescription.setConfirmedAt(LocalDateTime.now(ZoneOffset.UTC));

        when(medicationChartRepository.findByIdAndHospitalId(chartId, hospitalId))
                .thenReturn(Optional.of(chart));
        when(prescriptionRepository.findByIdAndHospitalId(prescriptionId, hospitalId))
                .thenReturn(Optional.of(prescription));
        when(medicationTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
