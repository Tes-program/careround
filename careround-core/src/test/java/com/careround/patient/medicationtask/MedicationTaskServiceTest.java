package com.careround.patient.medicationtask;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationtask.dto.TaskListResponse;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationTaskServiceTest {

    @Mock private MedicationTaskRepository medicationTaskRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicationChartRepository medicationChartRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private UserRepository userRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private MedicationTaskServiceImpl medicationTaskService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String WARD_ID = "ward-1";
    private static final String TASK_ID = "task-1";
    private static final String USER_ID = "user-nurse";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, USER_ID, UserRole.NURSE);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void getTaskList_partitionsCorrectly_overdue_dueSoon_upcoming() {
        MedicationTask overdueTask = task(TASK_ID + "-o", MedicationTaskStatus.OVERDUE, utcNow().minusHours(1));
        MedicationTask dueSoonTask = task(TASK_ID + "-d", MedicationTaskStatus.PENDING, utcNow().plusMinutes(20));
        MedicationTask upcomingTask = task(TASK_ID + "-u", MedicationTaskStatus.PENDING, utcNow().plusHours(2));
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(
                WARD_ID, HOSPITAL_ID, List.of(MedicationTaskStatus.PENDING, MedicationTaskStatus.OVERDUE)))
                .thenReturn(List.of(overdueTask, dueSoonTask, upcomingTask));
        stubCompletedTasks(List.of());

        TaskListResponse result = medicationTaskService.getTaskList(WARD_ID);

        assertThat(result.overdue()).hasSize(1);
        assertThat(result.dueSoon()).hasSize(1);
        assertThat(result.upcoming()).hasSize(1);
        assertThat(result.completed()).isEmpty();
    }

    @Test
    void getTaskList_returnsEmpty_whenNoTasks() {
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());
        stubCompletedTasks(List.of());

        TaskListResponse result = medicationTaskService.getTaskList(WARD_ID);

        assertThat(result.overdue()).isEmpty();
        assertThat(result.dueSoon()).isEmpty();
        assertThat(result.upcoming()).isEmpty();
        assertThat(result.completed()).isEmpty();
    }

    @Test
    void getTaskList_overdueTask_appearsInOverdueBucket() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.OVERDUE, utcNow().minusHours(3));
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of(t));
        stubCompletedTasks(List.of());

        TaskListResponse result = medicationTaskService.getTaskList(WARD_ID);

        assertThat(result.overdue()).hasSize(1);
        assertThat(result.dueSoon()).isEmpty();
        assertThat(result.upcoming()).isEmpty();
    }

    @Test
    void getTaskList_taskDueIn20Min_appearsInDueSoon() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.PENDING, utcNow().plusMinutes(20));
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of(t));
        stubCompletedTasks(List.of());

        TaskListResponse result = medicationTaskService.getTaskList(WARD_ID);

        assertThat(result.dueSoon()).hasSize(1);
        assertThat(result.overdue()).isEmpty();
        assertThat(result.upcoming()).isEmpty();
    }

    @Test
    void getTaskList_taskDueIn45Min_appearsInUpcoming() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.PENDING, utcNow().plusMinutes(45));
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of(t));
        stubCompletedTasks(List.of());

        TaskListResponse result = medicationTaskService.getTaskList(WARD_ID);

        assertThat(result.upcoming()).hasSize(1);
        assertThat(result.overdue()).isEmpty();
        assertThat(result.dueSoon()).isEmpty();
    }

    @Test
    void getTaskList_completedTasksToday_appearsInCompletedBucket() {
        MedicationTask completedTask = task(TASK_ID, MedicationTaskStatus.COMPLETED, utcNow().minusHours(1));
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());
        stubCompletedTasks(List.of(completedTask));

        TaskListResponse result = medicationTaskService.getTaskList(WARD_ID);

        assertThat(result.completed()).hasSize(1);
        assertThat(result.overdue()).isEmpty();
        assertThat(result.dueSoon()).isEmpty();
        assertThat(result.upcoming()).isEmpty();
    }

    @Test
    void complete_setsStatusCompleted_andTimestamp() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.PENDING, utcNow().plusHours(1));
        when(medicationTaskRepository.findByIdAndHospitalId(TASK_ID, HOSPITAL_ID)).thenReturn(Optional.of(t));
        when(medicationTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        medicationTaskService.complete(TASK_ID, null);

        assertThat(t.getStatus()).isEqualTo(MedicationTaskStatus.COMPLETED);
        assertThat(t.getCompletedAt()).isNotNull();
    }

    @Test
    void complete_throwsResourceNotFound_whenTaskNotFound() {
        when(medicationTaskRepository.findByIdAndHospitalId(TASK_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationTaskService.complete(TASK_ID, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void complete_throwsIllegalState_whenTaskAlreadyCompleted() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.COMPLETED, utcNow().minusHours(1));
        when(medicationTaskRepository.findByIdAndHospitalId(TASK_ID, HOSPITAL_ID)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> medicationTaskService.complete(TASK_ID, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void complete_worksForOverdueTask() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.OVERDUE, utcNow().minusHours(1));
        when(medicationTaskRepository.findByIdAndHospitalId(TASK_ID, HOSPITAL_ID)).thenReturn(Optional.of(t));
        when(medicationTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        medicationTaskService.complete(TASK_ID, null);

        assertThat(t.getStatus()).isEqualTo(MedicationTaskStatus.COMPLETED);
    }

    @Test
    void complete_setsCompletedByIdFromContext() {
        MedicationTask t = task(TASK_ID, MedicationTaskStatus.PENDING, utcNow().plusHours(1));
        when(medicationTaskRepository.findByIdAndHospitalId(TASK_ID, HOSPITAL_ID)).thenReturn(Optional.of(t));
        when(medicationTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        medicationTaskService.complete(TASK_ID, null);

        assertThat(t.getCompletedById()).isEqualTo(USER_ID);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private void stubCompletedTasks(List<MedicationTask> tasks) {
        when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusAndScheduledTimeBetweenOrderByScheduledTimeAsc(
                eq(WARD_ID), eq(HOSPITAL_ID), eq(MedicationTaskStatus.COMPLETED), any(), any()))
                .thenReturn(tasks);
    }

    private static LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private MedicationTask task(String id, MedicationTaskStatus status, LocalDateTime scheduledTime) {
        MedicationTask t = new MedicationTask();
        t.setId(id);
        t.setPatientId("patient-1");
        t.setHospitalId(HOSPITAL_ID);
        t.setWardId(WARD_ID);
        t.setMedicationChartId("chart-1");
        t.setScheduledTime(scheduledTime);
        t.setStatus(status);
        return t;
    }
}
