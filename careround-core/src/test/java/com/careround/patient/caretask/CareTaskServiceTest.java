package com.careround.patient.caretask;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.AssignedToRole;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskSource;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareTaskServiceTest {

    @Mock private CareTaskRepository careTaskRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private WardRepository wardRepository;
    @Mock private CareTaskAssignmentService careTaskAssignmentService;
    @Mock private OutboxService outboxService;
    @Spy private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks private CareTaskServiceImpl careTaskService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID = "patient-1";
    private static final String WARD_ID = "ward-1";
    private static final String TASK_ID = "task-1";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-1", UserRole.NURSE);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createTask_happyPath_returnsCareTaskResponse() {
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID, WARD_ID, PatientStatus.ADMITTED);
        var windowStart = java.time.LocalDateTime.now();
        var windowEnd = windowStart.plusMinutes(30);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        when(careTaskAssignmentService.assignForNewTask(HOSPITAL_ID, WARD_ID, windowStart, windowEnd))
                .thenReturn(new CareTaskAssignmentResult("nurse-1", false, null));
        when(careTaskRepository.save(any())).thenAnswer(inv -> {
            CareTask t = inv.getArgument(0);
            t.setId(TASK_ID);
            return t;
        });

        CareTaskResponse result = careTaskService.createTask(new CreateCareTaskRequest(
                PATIENT_ID, "Blood test", TaskSource.NURSING_CARE_PLAN,
                "Take blood sample", null, null, null, windowStart, windowEnd));

        assertThat(result.id()).isEqualTo(TASK_ID);
        assertThat(result.taskType()).isEqualTo("Blood test");
        assertThat(result.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(result.assignedToId()).isEqualTo("nurse-1");
        assertThat(result.assignedToRole()).isEqualTo(AssignedToRole.NURSE);
    }

    @Test
    void createTask_patientNotAdmitted_throwsBusinessRuleException() {
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID, WARD_ID, PatientStatus.DISCHARGED);
        var windowStart = java.time.LocalDateTime.now();
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> careTaskService.createTask(new CreateCareTaskRequest(
                PATIENT_ID, "Blood test", TaskSource.NURSING_CARE_PLAN,
                "Take blood sample", null, null, null, windowStart, windowStart.plusMinutes(30))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("admitted");

        verify(careTaskRepository, never()).save(any());
    }

    @Test
    void createTask_missingWindow_throwsBusinessRuleException() {
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID, WARD_ID, PatientStatus.ADMITTED);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> careTaskService.createTask(new CreateCareTaskRequest(
                PATIENT_ID, "Blood test", TaskSource.NURSING_CARE_PLAN,
                "Take blood sample", null, null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("time window");

        verify(careTaskRepository, never()).save(any());
    }

    @Test
    void assignTask_taskNotPending_throwsBusinessRuleException() {
        CareTask task = task(TASK_ID, HOSPITAL_ID, TaskStatus.IN_PROGRESS);
        when(careTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> careTaskService.assignTask(TASK_ID,
                new AssignTaskRequest("nurse-1", AssignedToRole.NURSE)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void assignTask_nurseWhoDidNotCreateTask_throwsAccessDeniedException() {
        CareTask task = task(TASK_ID, HOSPITAL_ID, TaskStatus.PENDING);
        task.setCreatedById("other-nurse");
        when(careTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> careTaskService.assignTask(TASK_ID,
                new AssignTaskRequest("nurse-2", AssignedToRole.NURSE)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("nurse who created");
    }

    @Test
    void assignTask_conflictingManualOverride_marksWorkloadConflict() {
        CareTask task = task(TASK_ID, HOSPITAL_ID, TaskStatus.PENDING);
        when(careTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(careTaskAssignmentService.hasConflictExcludingTask(
                HOSPITAL_ID, "nurse-2", TASK_ID, task.getWindowStart(), task.getWindowEnd()))
                .thenReturn(true);
        when(careTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));

        CareTaskResponse result = careTaskService.assignTask(TASK_ID,
                new AssignTaskRequest("nurse-2", AssignedToRole.NURSE));

        assertThat(result.assignedToId()).isEqualTo("nurse-2");
        assertThat(result.workloadConflict()).isTrue();
        assertThat(result.workloadConflictReason()).contains("Manual override");
    }

    @Test
    void progressTask_happyPath_setsInProgress() {
        CareTask task = task(TASK_ID, HOSPITAL_ID, TaskStatus.PENDING);
        when(careTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(careTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CareTaskResponse result = careTaskService.progressTask(TASK_ID);

        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void completeTask_taskNotInProgress_throwsBusinessRuleException() {
        CareTask task = task(TASK_ID, HOSPITAL_ID, TaskStatus.PENDING);
        when(careTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> careTaskService.completeTask(TASK_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("IN_PROGRESS");
    }

    @Test
    void getTasksByPatient_patientNotInHospital_throwsResourceNotFoundException() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careTaskService.getTasksByPatient(PATIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Patient patient(String id, String hospitalId, String wardId, PatientStatus status) {
        Patient p = new Patient();
        p.setId(id);
        p.setHospitalId(hospitalId);
        p.setWardId(wardId);
        p.setStatus(status);
        return p;
    }

    private CareTask task(String id, String hospitalId, TaskStatus status) {
        CareTask t = new CareTask();
        var windowStart = java.time.LocalDateTime.now();
        t.setId(id);
        t.setHospitalId(hospitalId);
        t.setPatientId(PATIENT_ID);
        t.setWardId(WARD_ID);
        t.setTaskType("Blood test");
        t.setSource(TaskSource.NURSING_CARE_PLAN);
        t.setTitle("Take blood sample");
        t.setStatus(status);
        t.setCreatedById("user-1");
        t.setWindowStart(windowStart);
        t.setWindowEnd(windowStart.plusMinutes(30));
        return t;
    }

    private Ward ward() {
        Ward ward = new Ward();
        ward.setId(WARD_ID);
        ward.setHospitalId(HOSPITAL_ID);
        ward.setSupervisorId("supervisor-1");
        return ward;
    }
}
