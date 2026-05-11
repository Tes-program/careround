package com.careround.patient.patient;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.DepartmentRepository;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.MarkDischargeReadyRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private WardRepository wardRepository;
    @Mock private MedicalTeamRepository medicalTeamRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private CareTaskRepository careTaskRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private PatientServiceImpl patientService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String USER_ID = "user-consultant";
    private static final String WARD_ID = "ward-1";
    private static final String TEAM_ID = "team-1";
    private static final String PATIENT_ID = "patient-1";

    private Ward ward;
    private MedicalTeam team;
    private Patient patient;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, USER_ID, UserRole.CONSULTANT);

        ward = new Ward();
        ward.setId(WARD_ID);
        ward.setHospitalId(HOSPITAL_ID);
        ward.setName("ICU");

        team = new MedicalTeam();
        team.setId(TEAM_ID);
        team.setHospitalId(HOSPITAL_ID);
        team.setName("Alpha Team");
        team.setDepartmentId("dept-1");
        team.setConsultantId(USER_ID);

        patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
        patient.setWardId(WARD_ID);
        patient.setMedicalTeamId(TEAM_ID);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setStatus(PatientStatus.ADMITTED);
        patient.setAdmissionDate(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void admitPatient_happyPath_returnsResponseAndPublishesEvent() {
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));
        when(medicalTeamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(patientRepository.save(any())).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(PATIENT_ID);
            return p;
        });

        AdmitPatientRequest request = admitRequest(USER_ID);
        PatientResponse result = patientService.admitPatient(request);

        assertThat(result.id()).isEqualTo(PATIENT_ID);
        assertThat(result.wardId()).isEqualTo(WARD_ID);
        verify(outboxService).publish(eq("careround.patient.admitted"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void admitPatient_wardNotFound_throwsNotFoundException() {
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.admitPatient(admitRequest(USER_ID)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ward not found");
    }

    @Test
    void admitPatient_wardBelongsToDifferentHospital_throwsAccessDeniedException() {
        Ward otherWard = new Ward();
        otherWard.setId(WARD_ID);
        otherWard.setHospitalId("other-hosp");
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(otherWard));

        assertThatThrownBy(() -> patientService.admitPatient(admitRequest(USER_ID)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void admitPatient_medicalTeamNotFound_throwsNotFoundException() {
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));
        when(medicalTeamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.admitPatient(admitRequest(USER_ID)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medical team not found");
    }

    @Test
    void admitPatient_noOnCallFound_throwsBusinessRuleException() {
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));
        when(medicalTeamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(departmentRepository.findAllByHospitalId(HOSPITAL_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> patientService.admitPatient(admitRequest(null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No on-call consultant found");
    }

    @Test
    void getPatient_crossTenantAccess_throwsAccessDeniedException() {
        Patient otherPatient = new Patient();
        otherPatient.setId(PATIENT_ID);
        otherPatient.setHospitalId("other-hosp");
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(otherPatient));

        assertThatThrownBy(() -> patientService.getPatient(PATIENT_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void markDischargeReady_nonConsultantRole_throwsAccessDeniedException() {
        HospitalContextHolder.set(HOSPITAL_ID, USER_ID, UserRole.NURSE);

        assertThatThrownBy(() -> patientService.markDischargeReady(PATIENT_ID, new MarkDischargeReadyRequest(null)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updatePatientStatus_dischargeWithPendingTasks_throwsBusinessRuleException() {
        patient.setStatus(PatientStatus.DISCHARGE_READY);
        patient.setDischargeReady(true);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patient));

        CareTask pendingTask = new CareTask();
        pendingTask.setStatus(TaskStatus.PENDING);
        when(careTaskRepository.findAllByPatientId(PATIENT_ID)).thenReturn(List.of(pendingTask));

        assertThatThrownBy(() -> patientService.updatePatientStatus(PATIENT_ID,
                new UpdatePatientStatusRequest(PatientStatus.DISCHARGED)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("incomplete care task");
    }

    @Test
    void updatePatientStatus_dischargeWithNoTasks_succeeds() {
        patient.setStatus(PatientStatus.DISCHARGE_READY);
        patient.setDischargeReady(true);
        patient.setBedNumber("4A");
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patient));
        when(careTaskRepository.findAllByPatientId(PATIENT_ID)).thenReturn(List.of());

        PatientResponse result = patientService.updatePatientStatus(PATIENT_ID,
                new UpdatePatientStatusRequest(PatientStatus.DISCHARGED));

        assertThat(result.status()).isEqualTo(PatientStatus.DISCHARGED);
        assertThat(result.wardId()).isNull();
        assertThat(result.bedNumber()).isNull();
    }

    private AdmitPatientRequest admitRequest(String consultantId) {
        return new AdmitPatientRequest(
                WARD_ID, TEAM_ID, "John", "Doe",
                LocalDate.of(1985, 6, 15), "M", "HN-001",
                AdmissionType.EMERGENCY, "Chest pain", "Cardiology",
                consultantId, null);
    }
}
