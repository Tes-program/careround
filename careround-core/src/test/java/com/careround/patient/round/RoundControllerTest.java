package com.careround.patient.round;

import com.careround.auth.enums.UserRole;
import com.careround.patient.enums.ClinicalStatus;
import com.careround.patient.enums.DischargeAssessment;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;
import com.careround.patient.round.dto.CreateRoundRequest;
import com.careround.patient.round.dto.PatientRoundReviewResponse;
import com.careround.patient.round.dto.ReviewPatientRequest;
import com.careround.patient.round.dto.RoundResponse;
import com.careround.shared.config.SecurityConfig;
import com.careround.shared.exception.BusinessRuleException;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoundController.class)
@Import(SecurityConfig.class)
class RoundControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private RoundService roundService;
    @MockitoBean private JwtService jwtService;

    private RoundResponse sampleRound;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.CONSULTANT);
        sampleRound = new RoundResponse("round-1", "hosp-1", "ward-1", "team-1", "shift-1",
                RoundType.MORNING, "doc-1", RoundStatus.SCHEDULED,
                null, null, null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createRound_asConsultant_returns201() throws Exception {
        when(roundService.createRound(any())).thenReturn(sampleRound);

        mockMvc.perform(post("/api/v1/rounds")
                        .with(user("cons").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRoundRequest("ward-1", "team-1", RoundType.MORNING,
                                        "doc-1", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    void createRound_asNurse_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/rounds")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRoundRequest("ward-1", "team-1", RoundType.MORNING,
                                        "doc-1", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRound_missingRequiredFields_returns400() throws Exception {
        String invalid = """
                {"wardId":"ward-1"}
                """;

        mockMvc.perform(post("/api/v1/rounds")
                        .with(user("cons").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startRound_duplicateInProgress_returns422() throws Exception {
        when(roundService.startRound("round-1"))
                .thenThrow(new BusinessRuleException("An active round of this type already exists"));

        mockMvc.perform(post("/api/v1/rounds/round-1/start")
                        .with(user("cons").roles("CONSULTANT")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getRounds_anyAuthenticatedUser_returns200() throws Exception {
        when(roundService.getRounds("ward-1", "team-1")).thenReturn(List.of(sampleRound));

        mockMvc.perform(get("/api/v1/rounds?wardId=ward-1&teamId=team-1")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("round-1"));
    }
}
