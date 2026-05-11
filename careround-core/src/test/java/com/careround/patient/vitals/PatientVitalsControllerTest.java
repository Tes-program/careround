package com.careround.patient.vitals;

import com.careround.auth.enums.UserRole;
import com.careround.patient.enums.ConsciousnessLevel;
import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientVitalsController.class)
@Import(SecurityConfig.class)
class PatientVitalsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PatientVitalsService patientVitalsService;
    @MockitoBean private JwtService jwtService;

    private VitalsResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.NURSE);
        sample = new VitalsResponse(
                "v-1", "p-1", "user-1",
                75, 16, 120,
                new BigDecimal("98.0"), new BigDecimal("37.0"),
                ConsciousnessLevel.ALERT, 0, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void recordVitals_asNurse_returns201() throws Exception {
        when(patientVitalsService.recordVitals(eq("p-1"), any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/patients/p-1/vitals")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.heartRate").value(75));
    }

    @Test
    void recordVitals_asAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/patients/p-1/vitals")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLatestVitals_patientNotFound_returns404() throws Exception {
        when(patientVitalsService.getLatestVitals("bad"))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/v1/patients/bad/vitals/latest")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void recordVitals_missingRequiredFields_returns400() throws Exception {
        String invalid = """
                {"heartRate":75}
                """;

        mockMvc.perform(post("/api/v1/patients/p-1/vitals")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    private RecordVitalsRequest sampleRequest() {
        return new RecordVitalsRequest(
                75, 16, new BigDecimal("98.0"),
                120, new BigDecimal("37.0"),
                ConsciousnessLevel.ALERT);
    }
}
