package com.careround.patient.clinicalnote;

import com.careround.auth.enums.UserRole;
import com.careround.patient.clinicalnote.dto.AmendNoteRequest;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.NoteType;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        HospitalContextHolder.set(HOSPITAL_ID, AUTHOR_ID, UserRole.JUNIOR_DOCTOR);
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
                new CreateClinicalNoteRequest(PATIENT_ID, NoteType.ROUND_NOTE, "Patient stable.", null));

        assertThat(result.id()).isEqualTo(NOTE_ID);
        assertThat(result.noteType()).isEqualTo(NoteType.ROUND_NOTE);
        assertThat(result.authorId()).isEqualTo(AUTHOR_ID);
    }

    @Test
    void createNote_patientNotFound_throwsResourceNotFoundException() {
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicalNoteService.createNote(
                new CreateClinicalNoteRequest(PATIENT_ID, NoteType.PROGRESS_NOTE, "Content", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void amendNote_notAuthor_throwsAccessDeniedException() {
        ClinicalNote note = note(NOTE_ID, PATIENT_ID, "other-user");
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID);

        when(clinicalNoteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> clinicalNoteService.amendNote(NOTE_ID,
                new AmendNoteRequest("Updated content")))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("author");
    }

    @Test
    void amendNote_happyPath_setsAmendedFields() {
        ClinicalNote note = note(NOTE_ID, PATIENT_ID, AUTHOR_ID);
        Patient patient = patient(PATIENT_ID, HOSPITAL_ID);

        when(clinicalNoteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.of(patient));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClinicalNoteResponse result = clinicalNoteService.amendNote(NOTE_ID,
                new AmendNoteRequest("Revised content"));

        assertThat(result.content()).isEqualTo("Revised content");
        assertThat(result.isAmended()).isTrue();
        assertThat(result.amendedById()).isEqualTo(AUTHOR_ID);
        assertThat(result.amendedAt()).isNotNull();
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
        n.setAuthorId(authorId);
        n.setNoteType(NoteType.ROUND_NOTE);
        n.setContent("Original content");
        return n;
    }
}
