package com.careround.hospital.oncall;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.oncall.dto.CreateOnCallRotationRequest;
import com.careround.hospital.oncall.dto.OnCallRotationResponse;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OnCallRotationController.class)
@Import(SecurityConfig.class)
class OnCallRotationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private OnCallRotationService onCallRotationService;
    @MockitoBean private JwtService jwtService;

    private OnCallRotationResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.ADMIN);
        sample = new OnCallRotationResponse("rot-1", "hosp-1", "dept-1", null, "doctor-1",
                OnCallRole.CONSULTANT_ON_CALL, LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(7), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void create_asAdmin_shouldReturn201() throws Exception {
        when(onCallRotationService.create(any(), any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/oncall")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOnCallRotationRequest(
                                "dept-1", null, "doctor-1", OnCallRole.CONSULTANT_ON_CALL,
                                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(7)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("CONSULTANT_ON_CALL"));
    }

    @Test
    void create_asNurse_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/oncall")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOnCallRotationRequest(
                                "dept-1", null, "doctor-1", OnCallRole.CONSULTANT_ON_CALL,
                                LocalDateTime.now(), LocalDateTime.now().plusHours(8)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_asAnyUser_shouldReturn200() throws Exception {
        when(onCallRotationService.listByHospital(any())).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/oncall")
                        .with(user("registrar").roles("REGISTRAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].doctorId").value("doctor-1"));
    }

    @Test
    void getCurrent_asAnyUser_shouldReturn200() throws Exception {
        when(onCallRotationService.getCurrentOnCall(any(), any(), any()))
                .thenReturn(Optional.of(sample));

        mockMvc.perform(get("/api/v1/oncall/current")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .param("departmentId", "dept-1")
                        .param("role", "CONSULTANT_ON_CALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.doctorId").value("doctor-1"));
    }

    @Test
    void delete_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/oncall/rot-1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void delete_asConsultant_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/oncall/rot-1")
                        .with(user("consultant").roles("CONSULTANT")))
                .andExpect(status().isForbidden());
    }
}
