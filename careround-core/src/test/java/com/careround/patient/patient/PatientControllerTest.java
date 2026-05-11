package com.careround.patient.patient;

import com.careround.auth.enums.UserRole;
import com.careround.patient.enums.AcuityLevel;
import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.shared.config.SecurityConfig;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import(SecurityConfig.class)
class PatientControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PatientService patientService;
    @MockitoBean private JwtService jwtService;

    private PatientResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.CONSULTANT);
        sample = new PatientResponse(
                "p-1", "ward-1", "team-1", "cons-1",
                "John", "Doe", "HN001", LocalDate.of(1985, 6, 15),
                "M", "4A", AdmissionType.EMERGENCY, "Chest pain", "Cardiology",
                AcuityLevel.LOW, 0, false, null, PatientStatus.ADMITTED,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void admitPatient_asConsultant_returns201() throws Exception {
        when(patientService.admitPatient(any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/patients")
                        .with(user("cons").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admitRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void admitPatient_asJuniorDoctor_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .with(user("jr").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admitRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPatient_anyAuthenticatedUser_returns200() throws Exception {
        when(patientService.getPatient("p-1")).thenReturn(sample);

        mockMvc.perform(get("/api/v1/patients/p-1")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("p-1"));
    }

    @Test
    void getPatient_notFound_returns404() throws Exception {
        when(patientService.getPatient("bad")).thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/v1/patients/bad")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void admitPatient_missingRequiredFields_returns400() throws Exception {
        String invalidJson = """
                {"wardId":"ward-1"}
                """;

        mockMvc.perform(post("/api/v1/patients")
                        .with(user("cons").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    private AdmitPatientRequest admitRequest() {
        return new AdmitPatientRequest(
                "ward-1", "team-1", "John", "Doe",
                LocalDate.of(1985, 6, 15), "M", "HN001",
                AdmissionType.EMERGENCY, "Chest pain", "Cardiology",
                "cons-1", null);
    }
}
