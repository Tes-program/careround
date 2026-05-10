package com.careround.hospital.department;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.department.dto.CreateDepartmentRequest;
import com.careround.hospital.department.dto.DepartmentResponse;
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

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
class DepartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private DepartmentService departmentService;
    @MockitoBean private JwtService jwtService;

    private DepartmentResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.ADMIN);
        sample = new DepartmentResponse("dept-1", "hosp-1", "Cardiology", null, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void create_asAdmin_shouldReturn201() throws Exception {
        when(departmentService.create(any(), any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/departments")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateDepartmentRequest("Cardiology", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Cardiology"));
    }

    @Test
    void create_asNurse_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/departments")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateDepartmentRequest("Cardiology", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_asAnyAuthenticatedUser_shouldReturn200() throws Exception {
        when(departmentService.listByHospital(any())).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/departments")
                        .with(user("registrar").roles("REGISTRAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Cardiology"));
    }

    @Test
    void delete_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/departments/dept-1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void delete_asConsultant_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/departments/dept-1")
                        .with(user("consultant").roles("CONSULTANT")))
                .andExpect(status().isForbidden());
    }
}
