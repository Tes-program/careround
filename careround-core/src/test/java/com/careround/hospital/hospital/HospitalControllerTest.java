package com.careround.hospital.hospital;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.hospital.dto.CreateHospitalRequest;
import com.careround.hospital.hospital.dto.HospitalResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HospitalController.class)
@Import(SecurityConfig.class)
class HospitalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private HospitalService hospitalService;
    @MockitoBean private JwtService jwtService;

    private HospitalResponse sampleResponse;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.ADMIN);
        sampleResponse = new HospitalResponse("hosp-1", "City Hospital", null,
                "admin@city.com", null, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void register_withValidPayload_shouldReturn201() throws Exception {
        when(hospitalService.register(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateHospitalRequest("City Hospital", null, "admin@city.com", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("hosp-1"))
                .andExpect(jsonPath("$.data.name").value("City Hospital"));
    }

    @Test
    void getMyHospital_withAuth_shouldReturn200() throws Exception {
        when(hospitalService.getById(any())).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/hospitals/me")
                        .with(user("user").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactEmail").value("admin@city.com"));
    }
}
