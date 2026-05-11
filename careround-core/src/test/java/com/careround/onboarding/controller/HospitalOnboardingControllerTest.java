package com.careround.onboarding.controller;

import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import com.careround.onboarding.service.HospitalOnboardingService;
import com.careround.shared.config.SecurityConfig;
import com.careround.shared.security.JwtService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HospitalOnboardingController.class)
@Import(SecurityConfig.class)
class HospitalOnboardingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private HospitalOnboardingService onboardingService;
    @MockitoBean private JwtService jwtService;

    @Test
    void submit_withoutAuth_shouldReturn201() throws Exception {
        HospitalOnboardingResponse response = sampleResponse();
        when(onboardingService.submit(any())).thenReturn(response);

        CreateHospitalOnboardingRequest request = new CreateHospitalOnboardingRequest(
                "City Hospital",
                "Nigeria",
                "admin@city.com",
                "+2348000000000",
                "General hospital",
                "120",
                "We need to digitise ward rounds and handovers.");

        mockMvc.perform(post("/api/v1/onboarding/hospital-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("req-1"))
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"));
    }

    @Test
    void list_asPlatformAdmin_shouldReturn200() throws Exception {
        when(onboardingService.list(eq(HospitalOnboardingStatus.PENDING_REVIEW), eq(20), any()))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/onboarding/hospital-requests")
                        .param("status", "PENDING_REVIEW")
                        .with(user("platform").roles("PLATFORM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("req-1"));
    }

    @Test
    void list_asTenantAdmin_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/hospital-requests")
                        .with(user("tenant-admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    private HospitalOnboardingResponse sampleResponse() {
        return new HospitalOnboardingResponse(
                "req-1",
                "City Hospital",
                "Nigeria",
                "admin@city.com",
                "+2348000000000",
                "General hospital",
                "120",
                "We need to digitise ward rounds and handovers.",
                HospitalOnboardingStatus.PENDING_REVIEW,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
