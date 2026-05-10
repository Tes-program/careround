package com.careround.hospital.shift;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.enums.ShiftType;
import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftController.class)
@Import(SecurityConfig.class)
class ShiftControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ShiftService shiftService;
    @MockitoBean private JwtService jwtService;

    private ShiftResponse activeShift;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.WARD_SUPERVISOR);
        activeShift = new ShiftResponse("shift-1", "ward-1", null,
                ShiftType.DAY, LocalDateTime.now(), LocalDateTime.now().plusHours(12),
                "doctor-1", "nurse-1", ShiftStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void assignStaff_asWardSupervisor_shouldReturn200() throws Exception {
        when(shiftService.assignStaff(any(), any(), any())).thenReturn(activeShift);

        mockMvc.perform(put("/api/v1/shifts/shift-1/assign")
                        .with(user("supervisor").roles("WARD_SUPERVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignStaffRequest("doctor-1", "nurse-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.leadDoctorId").value("doctor-1"));
    }

    @Test
    void assignStaff_asAdmin_shouldReturn200() throws Exception {
        when(shiftService.assignStaff(any(), any(), any())).thenReturn(activeShift);

        mockMvc.perform(put("/api/v1/shifts/shift-1/assign")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignStaffRequest("doctor-1", "nurse-1"))))
                .andExpect(status().isOk());
    }

    @Test
    void assignStaff_asNurse_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/shifts/shift-1/assign")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignStaffRequest("doctor-1", "nurse-1"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignStaff_asJuniorDoctor_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/shifts/shift-1/assign")
                        .with(user("junior").roles("JUNIOR_DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignStaffRequest("doctor-1", "nurse-1"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentShift_asAnyUser_shouldReturn200() throws Exception {
        when(shiftService.getCurrentShift(any(), any())).thenReturn(activeShift);

        mockMvc.perform(get("/api/v1/shifts/current/ward-1")
                        .with(user("registrar").roles("REGISTRAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wardId").value("ward-1"));
    }

    @Test
    void getCurrentShift_asConsultant_shouldReturn200() throws Exception {
        when(shiftService.getCurrentShift(any(), any())).thenReturn(activeShift);

        mockMvc.perform(get("/api/v1/shifts/current/ward-1")
                        .with(user("consultant").roles("CONSULTANT")))
                .andExpect(status().isOk());
    }
}
