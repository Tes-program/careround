package com.careround.hospital.shiftschedule;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.enums.ShiftType;
import com.careround.hospital.shiftschedule.dto.CreateShiftScheduleRequest;
import com.careround.hospital.shiftschedule.dto.ShiftScheduleResponse;
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
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftScheduleController.class)
@Import(SecurityConfig.class)
class ShiftScheduleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ShiftScheduleService shiftScheduleService;
    @MockitoBean private JwtService jwtService;

    private ShiftScheduleResponse sample;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.ADMIN);
        sample = new ShiftScheduleResponse("sched-1", "hosp-1", "ward-1",
                ShiftType.DAY, LocalTime.of(7, 0), LocalTime.of(19, 0),
                "MON,TUE,WED,THU,FRI", true, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void create_asAdmin_shouldReturn201() throws Exception {
        when(shiftScheduleService.create(any(), any())).thenReturn(sample);

        mockMvc.perform(post("/api/v1/shift-schedules")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateShiftScheduleRequest(
                                "ward-1", ShiftType.DAY, LocalTime.of(7, 0),
                                LocalTime.of(19, 0), "MON,TUE,WED,THU,FRI"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.shiftType").value("DAY"));
    }

    @Test
    void create_asWardSupervisor_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/shift-schedules")
                        .with(user("supervisor").roles("WARD_SUPERVISOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateShiftScheduleRequest(
                                "ward-1", ShiftType.DAY, LocalTime.of(7, 0),
                                LocalTime.of(19, 0), "MON,TUE,WED,THU,FRI"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listActive_asAnyUser_shouldReturn200() throws Exception {
        when(shiftScheduleService.listActive(any())).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/shift-schedules")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    void deactivate_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/v1/shift-schedules/sched-1/deactivate")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void deactivate_asConsultant_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/shift-schedules/sched-1/deactivate")
                        .with(user("consultant").roles("CONSULTANT")))
                .andExpect(status().isForbidden());
    }
}
