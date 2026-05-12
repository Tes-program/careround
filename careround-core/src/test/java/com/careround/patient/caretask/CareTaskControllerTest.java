package com.careround.patient.caretask;

import com.careround.auth.enums.UserRole;
import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.enums.AssignedToRole;
import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskSource;
import com.careround.patient.enums.TaskStatus;
import com.careround.shared.config.SecurityConfig;
import com.careround.shared.exception.BusinessRuleException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CareTaskController.class)
@Import(SecurityConfig.class)
class CareTaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private CareTaskService careTaskService;
    @MockitoBean private JwtService jwtService;

    private CareTaskResponse sampleTask;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        HospitalContextHolder.set("hosp-1", "user-1", UserRole.NURSE);
        sampleTask = new CareTaskResponse("task-1", "hosp-1", "patient-1", "ward-1",
                null, "user-1", null, null,
                "Blood test", TaskSource.NURSING_CARE_PLAN, "Take blood sample",
                null, TaskPriority.ROUTINE, now, now.plusMinutes(30),
                TaskStatus.PENDING, null, null, false, null,
                now, now);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createTask_asNurse_returns201() throws Exception {
        when(careTaskService.createTask(any())).thenReturn(sampleTask);

        mockMvc.perform(post("/api/v1/care-tasks")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCareTaskRequest("patient-1", "Blood test",
                                        TaskSource.NURSING_CARE_PLAN, "Take blood sample",
                                        null, null, null, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.taskType").value("Blood test"));
    }

    @Test
    void createTask_asAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/care-tasks")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCareTaskRequest("patient-1", "Blood test",
                                        TaskSource.NURSING_CARE_PLAN, "Take blood sample",
                                        null, null, null, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignTask_missingRequiredFields_returns400() throws Exception {
        String invalid = """
                {"assignedToId":"nurse-1"}
                """;

        mockMvc.perform(patch("/api/v1/care-tasks/task-1/assign")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void progressTask_taskNotFound_returns422() throws Exception {
        when(careTaskService.progressTask("bad-task"))
                .thenThrow(new BusinessRuleException("Only PENDING tasks can be progressed"));

        mockMvc.perform(patch("/api/v1/care-tasks/bad-task/progress")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getTasksByWard_anyAuthenticatedUser_returns200() throws Exception {
        when(careTaskService.getTasksByWard(eq("ward-1"), any())).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/api/v1/care-tasks/ward/ward-1")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("task-1"));
    }
}
