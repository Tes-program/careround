package com.careround.hospital.medicalteam;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.enums.InviteStatus;
import com.careround.hospital.medicalteam.dto.CreateMedicalTeamRequest;
import com.careround.hospital.medicalteam.dto.InviteResponse;
import com.careround.hospital.medicalteam.dto.MedicalTeamResponse;
import com.careround.hospital.medicalteam.dto.SendInviteRequest;
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

@WebMvcTest(MedicalTeamController.class)
@Import(SecurityConfig.class)
class MedicalTeamControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private MedicalTeamService medicalTeamService;
    @MockitoBean private JwtService jwtService;

    private MedicalTeamResponse sampleTeam;
    private InviteResponse sampleInvite;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hosp-1", "consultant-1", UserRole.CONSULTANT);
        sampleTeam = new MedicalTeamResponse("team-1", "hosp-1", "Alpha Team",
                "consultant-1", "dept-1", LocalDateTime.now());
        sampleInvite = new InviteResponse("invite-1", "hosp-1", "team-1",
                "user-target", "consultant-1", InviteStatus.PENDING,
                LocalDateTime.now().plusHours(48), LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void create_asConsultant_shouldReturn201() throws Exception {
        when(medicalTeamService.create(any(), any(), any())).thenReturn(sampleTeam);

        mockMvc.perform(post("/api/v1/teams")
                        .with(user("consultant").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMedicalTeamRequest("Alpha Team", "dept-1", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Alpha Team"));
    }

    @Test
    void create_asNurse_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                        .with(user("nurse").roles("NURSE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMedicalTeamRequest("Alpha Team", "dept-1", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_asAnyUser_shouldReturn200() throws Exception {
        when(medicalTeamService.listByHospital(any())).thenReturn(List.of(sampleTeam));

        mockMvc.perform(get("/api/v1/teams")
                        .with(user("registrar").roles("REGISTRAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Alpha Team"));
    }

    @Test
    void sendInvite_asConsultant_shouldReturn201() throws Exception {
        when(medicalTeamService.sendInvite(any(), any(), any(), any())).thenReturn(sampleInvite);

        mockMvc.perform(post("/api/v1/teams/team-1/invites")
                        .with(user("consultant").roles("CONSULTANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SendInviteRequest("user-target"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void sendInvite_asRegistrar_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/teams/team-1/invites")
                        .with(user("registrar").roles("REGISTRAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SendInviteRequest("user-target"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeMember_asConsultant_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/team-1/members/user-target")
                        .with(user("consultant").roles("CONSULTANT")))
                .andExpect(status().isOk());
    }

    @Test
    void removeMember_asNurse_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/team-1/members/user-target")
                        .with(user("nurse").roles("NURSE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPendingInvites_asAnyUser_shouldReturn200() throws Exception {
        when(medicalTeamService.listPendingInvites(any())).thenReturn(List.of(sampleInvite));

        mockMvc.perform(get("/api/v1/teams/invites/pending")
                        .with(user("registrar").roles("REGISTRAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void acceptInvite_asAnyUser_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/teams/invites/invite-1/accept")
                        .with(user("registrar").roles("REGISTRAR")))
                .andExpect(status().isOk());
    }
}
