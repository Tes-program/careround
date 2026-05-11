package com.careround.patient.caretask;

import com.careround.auth.enums.UserRole;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        when(careTaskRepository.save(any())).thenAnswer(inv -> {
            CareTask t = inv.getArgument(0);
            t.setId(TASK_ID);
            return t;
        });

        CareTaskResponse result = careTaskService.createTask(new CreateCareTaskRequest(
                PATIENT_ID, "Blood test", TaskSource.NURSING_CARE_PLAN,
                "Take blood sample", null, null, null, null, null));

        assertThat(result.id()).isEqualTo(TASK_ID);
        assertThat(result.taskType()).isEqualTo("Blood test");
        assertThat(result.status()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void createTask_patientNotAdmitted_throwsBusinessRuleException() {
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID, WARD_ID, PatientStatus.DISCHARGED);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> careTaskService.createTask(new CreateCareTaskRequest(
                PATIENT_ID, "Blood test", TaskSource.NURSING_CARE_PLAN,
                "Take blood sample", null, null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("admitted");

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
        t.setId(id);
        t.setHospitalId(hospitalId);
        t.setPatientId(PATIENT_ID);
        t.setWardId(WARD_ID);
        t.setTaskType("Blood test");
        t.setSource(TaskSource.NURSING_CARE_PLAN);
        t.setTitle("Take blood sample");
        t.setStatus(status);
        t.setCreatedById("user-1");
        return t;
    }
}
