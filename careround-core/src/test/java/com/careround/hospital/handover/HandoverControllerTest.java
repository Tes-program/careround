package com.careround.hospital.handover;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.enums.HandoverStatus;
import com.careround.hospital.handover.dto.HandoverResponse;
import com.careround.hospital.handover.dto.InitiateHandoverRequest;
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

@WebMvcTest(HandoverController.class)
@Import(SecurityConfig.class)
class HandoverControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private HandoverService handoverService;
    @MockitoBean private JwtService jwtService;

    private HandoverResponse sampleHandover;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.NURSE);
        sampleHandover = new HandoverResponse("hov-1", "ward-1", "shift-out", "shift-in",
                "user-1", HandoverStatus.PENDING, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void initiateHandover_asNurse_returns201() throws Exception {
        when(handoverService.initiateHandover(any())).thenReturn(sampleHandover);

        mockMvc.perform(post("/api/v1/handovers")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new InitiateHandoverRequest("ward-1", "shift-out", "shift-in", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void initiateHandover_asJuniorDoctor_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/handovers")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new InitiateHandoverRequest("ward-1", "shift-out", "shift-in", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void initiateHandover_missingRequiredFields_returns400() throws Exception {
        String invalid = """
                {"wardId":"ward-1"}
                """;

        mockMvc.perform(post("/api/v1/handovers")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeHandover_wardNotFound_returns404() throws Exception {
        when(handoverService.completeHandover(any(), any()))
                .thenThrow(new ResourceNotFoundException("Handover not found"));

        mockMvc.perform(post("/api/v1/handovers/bad-id/complete")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHandoversByWard_anyAuthenticatedUser_returns200() throws Exception {
        when(handoverService.getHandoversByWard("ward-1")).thenReturn(List.of(sampleHandover));

        mockMvc.perform(get("/api/v1/handovers/ward/ward-1")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("hov-1"));
    }
}
