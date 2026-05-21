package com.careround.patient.clinicalnote;

import com.careround.auth.enums.UserRole;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.NoteType;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicalNoteServiceTest {

    @Mock private ClinicalNoteRepository clinicalNoteRepository;
    @Mock private PatientRepository patientRepository;

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

    @Test
    void createNote_happyPath_returnsNoteResponse() {
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
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
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
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
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID);
        ClinicalNote note = note(NOTE_ID, PATIENT_ID, AUTHOR_ID);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        when(clinicalNoteRepository.findAllByPatientIdOrderByCreatedAtDesc(PATIENT_ID)).thenReturn(List.of(note));

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

    private Patient patient(String id, String hospitalId) {
        Patient p = new Patient();
        p.setId(id);
        p.setHospitalId(hospitalId);
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
}
