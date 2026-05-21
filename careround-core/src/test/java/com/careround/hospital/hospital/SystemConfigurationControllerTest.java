package com.careround.hospital.hospital;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.hospital.hospital.dto.UpdateSystemConfigRequest;
import com.careround.shared.config.SecurityConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemConfigurationController.class)
@Import(SecurityConfig.class)
class SystemConfigurationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private SystemConfigurationService systemConfigurationService;
    @MockitoBean private JwtService jwtService;

    private SystemConfigResponse sampleConfig;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.ADMIN);
        sampleConfig = new SystemConfigResponse("cfg-1", "hosp-1", 5, 7, 10, 20, true);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void getConfig_asAdmin_shouldReturn200() throws Exception {
        when(systemConfigurationService.getByHospitalId(any())).thenReturn(sampleConfig);

        mockMvc.perform(get("/api/v1/system-config")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acuityAmberThreshold").value(5));
    }

    @Test
    void getConfig_asNurse_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/system-config")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateConfig_asAdmin_shouldReturn200() throws Exception {
        when(systemConfigurationService.update(any(), any())).thenReturn(sampleConfig);

        mockMvc.perform(put("/api/v1/system-config")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateSystemConfigRequest(6, 8, 45, 30, true))))
                .andExpect(status().isOk());
    }

    @Test
    void updateConfig_asDoctor_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/system-config")
                        .with(user("doctor").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateSystemConfigRequest(6, 8, 45, 30, true))))
                .andExpect(status().isForbidden());
    }
}
