package com.careround.patient.nextofkin;

import com.careround.auth.enums.UserRole;
import com.careround.patient.enums.ContactMethod;
import com.careround.patient.nextofkin.dto.AddNextOfKinRequest;
import com.careround.patient.nextofkin.dto.NextOfKinResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NextOfKinController.class)
@Import(SecurityConfig.class)
class NextOfKinControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private NextOfKinService nextOfKinService;
    @MockitoBean private JwtService jwtService;

    private NextOfKinResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.NURSE);
        sample = new NextOfKinResponse(
                "nok-1", "p-1", "Jane Doe", "Spouse",
                "07700000001", null, ContactMethod.SMS,
                true, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void addNextOfKin_asNurse_returns201() throws Exception {
        when(nextOfKinService.addNextOfKin(eq("p-1"), any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/patients/p-1/next-of-kin")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Jane Doe"));
    }

    @Test
    void addNextOfKin_missingRequiredFields_returns400() throws Exception {
        // include primitive boolean fields to avoid Jackson3 parse error; omit required String/enum fields
        String invalid = """
                {"name":"Jane","isEmergencyContact":false,"notificationConsent":false}
                """;

        mockMvc.perform(post("/api/v1/patients/p-1/next-of-kin")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNextOfKin_anyAuthenticatedUser_returns200() throws Exception {
        when(nextOfKinService.getNextOfKin("p-1")).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/patients/p-1/next-of-kin")
                        .with(user("junior").roles("JUNIOR_DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("nok-1"));
    }

    @Test
    void removeNextOfKin_asJuniorDoctor_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/p-1/next-of-kin/nok-1")
                        .with(user("junior").roles("JUNIOR_DOCTOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeNextOfKin_nokNotFound_returns404() throws Exception {
        when(nextOfKinService.getNextOfKin("p-1"))
                .thenThrow(new ResourceNotFoundException("Next of kin not found"));

        mockMvc.perform(get("/api/v1/patients/p-1/next-of-kin")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isNotFound());
    }

    private AddNextOfKinRequest sampleRequest() {
        return new AddNextOfKinRequest(
                "Jane Doe", "Spouse", "07700000001", null,
                ContactMethod.SMS, true, true);
    }
}
