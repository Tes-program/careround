package com.careround.scheduler.jobs;

import com.careround.auth.entity.User;
import com.careround.auth.repository.UserRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.repository.PatientRepository;
import com.careround.scheduler.service.MedicationTaskReminderProcessor;
import com.careround.shared.event.MedicationTaskOverdueEvent;
import com.careround.shared.event.MedicationTaskReminderEvent;
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
    @Mock private PrescriptionRepository    prescriptionRepository;
    @Mock private PatientRepository         patientRepository;
    @Mock private UserRepository            userRepository;
    @Mock private OutboxService             outboxService;

    @InjectMocks private MedicationTaskReminderProcessor processor;

    // ─── processOverdue tests ────────────────────────────────────────────────

    @Test
    void processOverdue_marksOverdue_andSetsOverdueAlertSentAt() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");
        stubPatientAndNurse("patient-1", "hosp-1", "nurse-1");

        processor.processOverdue();

        assertThat(task.getStatus()).isEqualTo(MedicationTaskStatus.OVERDUE);
        assertThat(task.getOverdueAlertSentAt()).isNotNull();
    }

    @Test
    void processOverdue_returnsCountOfProcessed() {
        MedicationTask t1 = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        MedicationTask t2 = pendingTask("task-2", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        stubOverdueQuery(t1, t2);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");
        stubPatientAndNurse("patient-1", "hosp-1", "nurse-1");

        int count = processor.processOverdue();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void processOverdue_doesNothing_whenNoOverdueTasks() {
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndOverdueAlertSentAtIsNull(
                any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        int count = processor.processOverdue();

        assertThat(count).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void processOverdue_computesMinutesOverdue_correctly() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(45));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");
        stubPatientAndNurse("patient-1", "hosp-1", "nurse-1");

        ArgumentCaptor<MedicationTaskOverdueEvent> captor =
                ArgumentCaptor.forClass(MedicationTaskOverdueEvent.class);
        processor.processOverdue();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().minutesOverdue()).isGreaterThanOrEqualTo(44);
    }

    @Test
    void processOverdue_publishesToCorrectTopic() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");
        stubPatientAndNurse("patient-1", "hosp-1", "nurse-1");

        processor.processOverdue();

        verify(outboxService).publish(eq("medication-task-overdue"), any(), eq("hosp-1"));
    }

    @Test
    void processOverdue_usesHospitalIdFromTask() {
        MedicationTask task = pendingTask("task-1", "hosp-specific", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-specific");
        stubPatientAndNurse("patient-1", "hosp-specific", "nurse-1");

        HospitalContextHolder.clear();
        processor.processOverdue();

        verify(outboxService).publish(any(), any(), eq("hosp-specific"));
    }

    @Test
    void processOverdue_includesDrugName_inEvent() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");
        stubPatientAndNurse("patient-1", "hosp-1", "nurse-1");

        ArgumentCaptor<MedicationTaskOverdueEvent> captor =
                ArgumentCaptor.forClass(MedicationTaskOverdueEvent.class);
        processor.processOverdue();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().drugName()).isEqualTo("Aspirin");
        assertThat(captor.getValue().dose()).isEqualTo("100mg");
    }

    @Test
    void processOverdue_includesPatientName_andDeviceToken_inEvent() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");
        stubPatientAndNurse("patient-1", "hosp-1", "nurse-1");

        ArgumentCaptor<MedicationTaskOverdueEvent> captor =
                ArgumentCaptor.forClass(MedicationTaskOverdueEvent.class);
        processor.processOverdue();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().patientName()).isEqualTo("Alice Smith");
        assertThat(captor.getValue().deviceToken()).isEqualTo("fcm-token-nurse-1");
    }

    @Test
    void processOverdue_setsNullPatientName_whenPatientNotFound() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stubOverdueQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        when(patientRepository.findByIdAndHospitalId("patient-1", "hosp-1"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdAndHospitalId("nurse-1", "hosp-1"))
                .thenReturn(Optional.empty());

        ArgumentCaptor<MedicationTaskOverdueEvent> captor =
                ArgumentCaptor.forClass(MedicationTaskOverdueEvent.class);
        processor.processOverdue();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().patientName()).isNull();
        assertThat(captor.getValue().deviceToken()).isNull();
    }

    // ─── processReminders tests ──────────────────────────────────────────────

    @Test
    void processReminders_setsPreReminderSentAt_andPublishesReminderEvent() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15));
        stubReminderQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        processor.processReminders();

        assertThat(task.getPreReminderSentAt()).isNotNull();
        verify(outboxService).publish(eq("medication-task-reminder"), any(), eq("hosp-1"));
    }

    @Test
    void processReminders_doesNothing_whenNoTasksDueSoon() {
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBetweenAndPreReminderSentAtIsNull(
                any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        int count = processor.processReminders();

        assertThat(count).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void processReminders_includesDrugName_inEvent() {
        MedicationTask task = pendingTask("task-1", "hosp-1", "chart-1",
                LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15));
        stubReminderQuery(task);
        setupChartAndPrescription("chart-1", "rx-1", "hosp-1");

        ArgumentCaptor<MedicationTaskReminderEvent> captor =
                ArgumentCaptor.forClass(MedicationTaskReminderEvent.class);
        processor.processReminders();

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().drugName()).isEqualTo("Aspirin");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private MedicationTask pendingTask(String id, String hospitalId, String chartId,
                                       LocalDateTime scheduledTime) {
        MedicationTask t = new MedicationTask();
        t.setId(id);
        t.setPatientId("patient-1");
        t.setHospitalId(hospitalId);
        t.setWardId("ward-1");
        t.setAssignedNurseId("nurse-1");
        t.setMedicationChartId(chartId);
        t.setScheduledTime(scheduledTime);
        t.setStatus(MedicationTaskStatus.PENDING);
        return t;
    }

    private void stubOverdueQuery(MedicationTask... tasks) {
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndOverdueAlertSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tasks)))
                .thenReturn(new PageImpl<>(List.of()));
        when(medicationTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubReminderQuery(MedicationTask... tasks) {
        when(medicationTaskRepository.findAllByStatusAndScheduledTimeBetweenAndPreReminderSentAtIsNull(
                eq(MedicationTaskStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tasks)))
                .thenReturn(new PageImpl<>(List.of()));
        when(medicationTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
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
    }

    private void stubPatientAndNurse(String patientId, String hospitalId, String nurseId) {
        Patient patient = new Patient();
        patient.setFirstName("Alice");
        patient.setLastName("Smith");
        when(patientRepository.findByIdAndHospitalId(patientId, hospitalId))
                .thenReturn(Optional.of(patient));

        User nurse = new User();
        nurse.setFcmToken("fcm-token-" + nurseId);
        when(userRepository.findByIdAndHospitalId(nurseId, hospitalId))
                .thenReturn(Optional.of(nurse));
    }
}
