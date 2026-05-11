package com.careround.patient.escalation;

import com.careround.auth.enums.UserRole;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.EscalationTrigger;
import com.careround.patient.escalation.dto.CreateEscalationRequest;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.patient.escalation.dto.ResolveEscalationRequest;
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

@WebMvcTest(EscalationController.class)
@Import(SecurityConfig.class)
class EscalationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private EscalationService escalationService;
    @MockitoBean private JwtService jwtService;

    private EscalationResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.NURSE);
        sample = new EscalationResponse(
                "esc-1", "p-1", "hosp-1", "user-1", null,
                EscalationTrigger.NURSE_CONCERN, EscalationSeverity.AMBER,
                EscalationStatus.OPEN, "Concerned about patient",
                null, LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createManualEscalation_asNurse_returns201() throws Exception {
        when(escalationService.createManualEscalation(any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/escalations")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEscalationRequest("p-1", EscalationSeverity.AMBER,
                                        EscalationTrigger.NURSE_CONCERN, "Concerned"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.severity").value("AMBER"));
    }

    @Test
    void createManualEscalation_asConsultant_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/escalations")
                        .with(user("cons").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEscalationRequest("p-1", EscalationSeverity.AMBER,
                                        EscalationTrigger.NURSE_CONCERN, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOpenEscalations_anyAuthenticatedUser_returns200() throws Exception {
        when(escalationService.getOpenEscalations("ward-1")).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/escalations/ward/ward-1")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("esc-1"));
    }

    @Test
    void resolveEscalation_missingNotes_returns400() throws Exception {
        String invalid = """
                {}
                """;

        mockMvc.perform(patch("/api/v1/escalations/esc-1/resolve")
                        .with(user("registrar").roles("REGISTRAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEscalationsByPatient_notFound_returns404() throws Exception {
        when(escalationService.getEscalationsByPatient("bad"))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/v1/escalations/patient/bad")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isNotFound());
    }
}
