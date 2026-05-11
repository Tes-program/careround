package com.careround.patient.escalation;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.patient.entity.Escalation;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.EscalationTrigger;
import com.careround.patient.escalation.dto.AcknowledgeEscalationRequest;
import com.careround.patient.escalation.dto.CreateEscalationRequest;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.patient.escalation.dto.ResolveEscalationRequest;
import com.careround.patient.repository.EscalationRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscalationServiceTest {

    @Mock private EscalationRepository escalationRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicalTeamRepository medicalTeamRepository;
    @Mock private OnCallRotationRepository onCallRotationRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private EscalationServiceImpl escalationService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID = "patient-1";
    private static final String DEPT_ID = "dept-1";
    private static final String DOCTOR_ID = "doctor-registrar";

    private Patient patient;
    private MedicalTeam team;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-1", UserRole.NURSE);

        patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
        patient.setWardId("ward-1");
        patient.setMedicalTeamId("team-1");
        patient.setNewsScore(6);

        team = new MedicalTeam();
        team.setId("team-1");
        team.setHospitalId(HOSPITAL_ID);
        team.setDepartmentId(DEPT_ID);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void triggerSystemEscalation_amber_assignsRegistrar() {
        when(escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                eq(PATIENT_ID), eq(EscalationStatus.OPEN), any()))
                .thenReturn(Optional.empty());
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patient));
        when(medicalTeamRepository.findByIdAndHospitalId("team-1", HOSPITAL_ID))
                .thenReturn(Optional.of(team));

        OnCallRotation onCall = new OnCallRotation();
        onCall.setDoctorId(DOCTOR_ID);
        onCall.setRole(OnCallRole.REGISTRAR_ON_CALL);
        when(onCallRotationRepository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                eq(HOSPITAL_ID), eq(DEPT_ID), eq(OnCallRole.REGISTRAR_ON_CALL), any(), any()))
                .thenReturn(Optional.of(onCall));

        when(escalationRepository.save(any())).thenAnswer(inv -> {
            Escalation e = inv.getArgument(0);
            e.setId("esc-1");
            return e;
        });

        EscalationResponse result = escalationService.triggerSystemEscalation(
                PATIENT_ID, EscalationSeverity.AMBER, "Score 6");

        assertThat(result.assignedToId()).isEqualTo(DOCTOR_ID);
        assertThat(result.severity()).isEqualTo(EscalationSeverity.AMBER);
    }

    @Test
    void triggerSystemEscalation_red_assignsConsultant() {
        when(escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                eq(PATIENT_ID), eq(EscalationStatus.OPEN), any()))
                .thenReturn(Optional.empty());
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patient));
        when(medicalTeamRepository.findByIdAndHospitalId("team-1", HOSPITAL_ID))
                .thenReturn(Optional.of(team));

        OnCallRotation onCall = new OnCallRotation();
        onCall.setDoctorId("doctor-consultant");
        when(onCallRotationRepository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                eq(HOSPITAL_ID), eq(DEPT_ID), eq(OnCallRole.CONSULTANT_ON_CALL), any(), any()))
                .thenReturn(Optional.of(onCall));

        when(escalationRepository.save(any())).thenAnswer(inv -> {
            Escalation e = inv.getArgument(0);
            e.setId("esc-1");
            return e;
        });

        EscalationResponse result = escalationService.triggerSystemEscalation(
                PATIENT_ID, EscalationSeverity.RED, "Score 9");

        assertThat(result.assignedToId()).isEqualTo("doctor-consultant");
        assertThat(result.severity()).isEqualTo(EscalationSeverity.RED);
    }

    @Test
    void triggerSystemEscalation_openEscalationExists_returnsDuplicate() {
        Escalation existing = buildEscalation("esc-existing", EscalationSeverity.RED, EscalationStatus.OPEN);
        when(escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                eq(PATIENT_ID), eq(EscalationStatus.OPEN), any()))
                .thenReturn(Optional.of(existing));

        EscalationResponse result = escalationService.triggerSystemEscalation(
                PATIENT_ID, EscalationSeverity.RED, "Score 9");

        assertThat(result.id()).isEqualTo("esc-existing");
        verify(escalationRepository, never()).save(any());
    }

    @Test
    void triggerSystemEscalation_noOnCallFound_createsEscalationWithNullAssignee() {
        when(escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                eq(PATIENT_ID), eq(EscalationStatus.OPEN), any()))
                .thenReturn(Optional.empty());
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patient));
        when(medicalTeamRepository.findByIdAndHospitalId("team-1", HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(onCallRotationRepository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(escalationRepository.save(any())).thenAnswer(inv -> {
            Escalation e = inv.getArgument(0);
            e.setId("esc-1");
            return e;
        });

        EscalationResponse result = escalationService.triggerSystemEscalation(
                PATIENT_ID, EscalationSeverity.AMBER, "Score 5");

        assertThat(result.assignedToId()).isNull();
    }

    @Test
    void createManualEscalation_happyPath_publishesDeteriorationEvent() {
        when(escalationRepository.findFirstByPatientIdAndStatusAndSeverityIn(
                eq(PATIENT_ID), eq(EscalationStatus.OPEN), any()))
                .thenReturn(Optional.empty());
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patient));
        when(medicalTeamRepository.findByIdAndHospitalId("team-1", HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(onCallRotationRepository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(escalationRepository.save(any())).thenAnswer(inv -> {
            Escalation e = inv.getArgument(0);
            e.setId("esc-1");
            return e;
        });

        EscalationResponse result = escalationService.createManualEscalation(
                new CreateEscalationRequest(PATIENT_ID, EscalationSeverity.AMBER, EscalationTrigger.NURSE_CONCERN, "Concerned"));

        assertThat(result.id()).isEqualTo("esc-1");
        verify(outboxService).publish(eq("careround.patient.deterioration"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void acknowledgeEscalation_crossTenantAccess_throwsAccessDeniedException() {
        when(escalationRepository.findByIdAndHospitalId("esc-1", HOSPITAL_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> escalationService.acknowledgeEscalation("esc-1",
                new AcknowledgeEscalationRequest("notes")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveEscalation_happyPath_setsResolvedAt() {
        Escalation escalation = buildEscalation("esc-1", EscalationSeverity.RED, EscalationStatus.ACKNOWLEDGED);
        when(escalationRepository.findByIdAndHospitalId("esc-1", HOSPITAL_ID))
                .thenReturn(Optional.of(escalation));

        EscalationResponse result = escalationService.resolveEscalation("esc-1",
                new ResolveEscalationRequest("Resolved after review"));

        assertThat(result.status()).isEqualTo(EscalationStatus.RESOLVED);
        assertThat(escalation.getResolvedAt()).isNotNull();
    }

    private Escalation buildEscalation(String id, EscalationSeverity severity, EscalationStatus status) {
        Escalation e = new Escalation();
        e.setId(id);
        e.setHospitalId(HOSPITAL_ID);
        e.setPatientId(PATIENT_ID);
        e.setSeverity(severity);
        e.setStatus(status);
        e.setTriggerType(EscalationTrigger.HIGH_NEWS_SCORE);
        return e;
    }
}
