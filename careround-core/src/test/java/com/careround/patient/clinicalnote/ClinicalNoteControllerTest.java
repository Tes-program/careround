package com.careround.patient.clinicalnote;

import com.careround.auth.enums.UserRole;
import com.careround.patient.clinicalnote.dto.AmendNoteRequest;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.enums.NoteType;
import com.careround.shared.config.SecurityConfig;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClinicalNoteController.class)
@Import(SecurityConfig.class)
class ClinicalNoteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ClinicalNoteService clinicalNoteService;
    @MockitoBean private JwtService jwtService;

    private ClinicalNoteResponse sampleNote;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.JUNIOR_DOCTOR);
        sampleNote = new ClinicalNoteResponse("note-1", "patient-1", null, "user-1",
                NoteType.ROUND_NOTE, "Patient is stable.", false, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createNote_asJuniorDoctor_returns201() throws Exception {
        when(clinicalNoteService.createNote(any())).thenReturn(sampleNote);

        mockMvc.perform(post("/api/v1/clinical-notes")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateClinicalNoteRequest("patient-1", NoteType.ROUND_NOTE,
                                        "Patient is stable.", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.noteType").value("ROUND_NOTE"));
    }

    @Test
    void createNote_missingContent_returns400() throws Exception {
        String invalid = """
                {"patientId":"patient-1","noteType":"ROUND_NOTE"}
                """;

        mockMvc.perform(post("/api/v1/clinical-notes")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendNote_notAuthor_returns403() throws Exception {
        when(clinicalNoteService.amendNote(eq("note-1"), any()))
                .thenThrow(new AccessDeniedException("Only the note author can amend this note"));

        mockMvc.perform(patch("/api/v1/clinical-notes/note-1/amend")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmendNoteRequest("Revised"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPatientNotes_patientNotFound_returns404() throws Exception {
        when(clinicalNoteService.getPatientNotes("bad-patient"))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/v1/clinical-notes/patient/bad-patient")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPatientNotes_anyAuthenticatedUser_returns200() throws Exception {
        when(clinicalNoteService.getPatientNotes("patient-1")).thenReturn(List.of(sampleNote));

        mockMvc.perform(get("/api/v1/clinical-notes/patient/patient-1")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("note-1"));
    }
}
