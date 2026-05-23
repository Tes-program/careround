package com.careround.patient.clinicalnote;

import com.careround.auth.enums.UserRole;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.ConfirmNoteRequest;
import com.careround.patient.clinicalnote.dto.ConfirmNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.NoteType;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.dto.CreatePrescriptionRequest;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.ai.client.AiServiceClient;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicalNoteServiceTest {

    @Mock private ClinicalNoteRepository clinicalNoteRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private OutboxService outboxService;
    @Mock private AiServiceClient aiServiceClient;

    @InjectMocks private ClinicalNoteServiceImpl clinicalNoteService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID = "patient-1";
    private static final String NOTE_ID = "note-1";
    private static final String AUTHOR_ID = "user-1";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, AUTHOR_ID, UserRole.DOCTOR);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    // ─── createNote ──────────────────────────────────────────────────────────

    @Test
    void createNote_happyPath_returnsNoteResponse() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });

        ClinicalNoteResponse result = clinicalNoteService.createNote(
                new CreateClinicalNoteRequest(PATIENT_ID, NoteType.WARD_ROUND_NOTE, "Patient stable.", null, false, null));

        assertThat(result.id()).isEqualTo(NOTE_ID);
        assertThat(result.noteType()).isEqualTo(NoteType.WARD_ROUND_NOTE);
        assertThat(result.authorId()).isEqualTo(AUTHOR_ID);
        assertThat(result.hospitalId()).isEqualTo(HOSPITAL_ID);
        assertThat(result.isAiGenerated()).isFalse();
    }

    @Test
    void createNote_aiGenerated_setsAiFields() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });

        ClinicalNoteResponse result = clinicalNoteService.createNote(
                new CreateClinicalNoteRequest(PATIENT_ID, NoteType.PROGRESS_NOTE,
                        "AI summary", "raw transcript", true, "claude-sonnet-4-6"));

        assertThat(result.isAiGenerated()).isTrue();
        assertThat(result.aiModelUsed()).isEqualTo("claude-sonnet-4-6");
        assertThat(result.rawTranscription()).isEqualTo("raw transcript");
    }

    @Test
    void createNote_patientNotFound_throwsResourceNotFoundException() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicalNoteService.createNote(
                new CreateClinicalNoteRequest(PATIENT_ID, NoteType.PROGRESS_NOTE, "Content", null, false, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPatientNotes_returnsAllNotes() {
        ClinicalNote note = note(NOTE_ID, PATIENT_ID, AUTHOR_ID);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc(PATIENT_ID, HOSPITAL_ID)).thenReturn(List.of(note));

        List<ClinicalNoteResponse> results = clinicalNoteService.getPatientNotes(PATIENT_ID);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().id()).isEqualTo(NOTE_ID);
    }

    @Test
    void getPatientNotes_patientNotFound_throwsResourceNotFoundException() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicalNoteService.getPatientNotes(PATIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── confirm ─────────────────────────────────────────────────────────────

    @Test
    void confirm_savesClinicalNote_withCorrectFields() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });

        ConfirmNoteResponse result = clinicalNoteService.confirm(confirmRequest(List.of()));

        assertThat(result.noteId()).isEqualTo(NOTE_ID);
        assertThat(result.prescriptionIds()).isEmpty();
        verify(clinicalNoteRepository).save(any());
    }

    @Test
    void confirm_savesAllPrescriptions_withConfirmedByAndAt() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId("rx-" + System.nanoTime());
            return p;
        });

        List<CreatePrescriptionRequest> prescriptions = List.of(prescriptionReq("Aspirin"), prescriptionReq("Atorvastatin"));

        ConfirmNoteResponse result = clinicalNoteService.confirm(confirmRequest(prescriptions));

        assertThat(result.prescriptionIds()).hasSize(2);
        verify(prescriptionRepository, times(2)).save(any(Prescription.class));
    }

    @Test
    void confirm_writesOutboxEvent_perPrescription() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId("rx-1");
            return p;
        });

        clinicalNoteService.confirm(confirmRequest(List.of(prescriptionReq("Aspirin"))));

        verify(outboxService).publish(eq("prescription-confirmed"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void confirm_writesClinicalNoteSavedEvent() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });

        clinicalNoteService.confirm(confirmRequest(List.of()));

        verify(outboxService).publish(eq("clinical-note-saved"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void confirm_setsAiFields_whenAiGenerated() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });

        ConfirmNoteRequest aiRequest = new ConfirmNoteRequest(
                PATIENT_ID, NoteType.PROGRESS_NOTE, "AI content", "raw voice",
                true, "claude-sonnet-4-6", false, List.of());

        clinicalNoteService.confirm(aiRequest);

        verify(clinicalNoteRepository).save(argThat(n -> n.isAiGenerated()
                && "claude-sonnet-4-6".equals(n.getAiModelUsed())
                && n.getConfirmedByDoctorAt() != null));
    }

    @Test
    void confirm_throwsResourceNotFound_whenPatientNotInHospital() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicalNoteService.confirm(confirmRequest(List.of())))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clinicalNoteRepository, never()).save(any());
    }

    @Test
    void confirm_worksWithEmptyPrescriptionList() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            n.setId(NOTE_ID);
            return n;
        });

        ConfirmNoteResponse result = clinicalNoteService.confirm(confirmRequest(List.of()));

        assertThat(result.prescriptionIds()).isEmpty();
        verify(prescriptionRepository, never()).save(any());
        verify(outboxService, never()).publish(eq("prescription-confirmed"), any(), any());
        verify(outboxService).publish(eq("clinical-note-saved"), any(), any());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Patient patient() {
        Patient p = new Patient();
        p.setId(PATIENT_ID);
        p.setHospitalId(HOSPITAL_ID);
        return p;
    }

    private ClinicalNote note(String id, String patientId, String authorId) {
        ClinicalNote n = new ClinicalNote();
        n.setId(id);
        n.setPatientId(patientId);
        n.setHospitalId(HOSPITAL_ID);
        n.setAuthorId(authorId);
        n.setNoteType(NoteType.WARD_ROUND_NOTE);
        n.setContent("Original content");
        return n;
    }

    private ConfirmNoteRequest confirmRequest(List<CreatePrescriptionRequest> prescriptions) {
        return new ConfirmNoteRequest(PATIENT_ID, NoteType.WARD_ROUND_NOTE,
                "Ward round note", null, false, null, false, prescriptions);
    }

    private CreatePrescriptionRequest prescriptionReq(String drug) {
        return new CreatePrescriptionRequest(
                drug, "100mg", "oral", "daily", 24, 7,
                LocalDateTime.now().plusHours(1),
                List.of(LocalDateTime.now().plusHours(1)));
    }

    private static <T> T argThat(java.util.function.Predicate<T> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
