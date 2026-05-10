package com.careround.hospital.ward;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WardController.class)
@Import(SecurityConfig.class)
class WardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private WardService wardService;
    @MockitoBean private JwtService jwtService;

    private WardResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.ADMIN);
        sample = new WardResponse("ward-1", "hosp-1", "ICU", "Critical Care", 10, null, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void create_asAdmin_shouldReturn201() throws Exception {
        when(wardService.create(any(), any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/wards")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateWardRequest("ICU", "Critical Care", 10, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("ICU"));
    }

    @Test
    void create_asJuniorDoctor_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/wards")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateWardRequest("ICU", null, 10, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_asNurse_shouldReturn200() throws Exception {
        when(wardService.listByHospital(any())).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/wards")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalBeds").value(10));
    }

    @Test
    void delete_asWardSupervisor_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/wards/ward-1")
                        .with(user("supervisor").roles("WARD_SUPERVISOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/wards/ward-1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
