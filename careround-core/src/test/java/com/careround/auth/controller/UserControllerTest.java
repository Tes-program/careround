package com.careround.auth.controller;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UserResponse;
import com.careround.auth.enums.UserRole;
import com.careround.auth.service.UserService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;

    private UserResponse sampleUser;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set("hospital-456", "admin-123", UserRole.ADMIN);
        sampleUser = new UserResponse(
                "user-123", "hospital-456", "Jane", "Doe",
                "jane.doe@hospital.com", UserRole.NURSE, null,
                true, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createUser_asAdmin_shouldReturn201() throws Exception {
        when(userService.create(any(), any())).thenReturn(sampleUser);

        mockMvc.perform(post("/api/v1/users")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(
                                "Jane", "Doe", "jane.doe@hospital.com",
                                "password123", UserRole.NURSE, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("jane.doe@hospital.com"))
                .andExpect(jsonPath("$.data.role").value("NURSE"));
    }

    @Test
    void createUser_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(
                                "Jane", "Doe", "jane.doe@hospital.com",
                                "password123", UserRole.NURSE, null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_withInvalidBearerToken_shouldReturn401() throws Exception {
        when(jwtService.isTokenValid("expired.access.token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer expired.access.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.message").value("Invalid or expired access token"));
    }

    @Test
    void getAllUsers_asConsultant_shouldReturn200WithList() throws Exception {
        when(userService.listByHospital(any())).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users")
                        .with(user("consultant").roles("CONSULTANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].firstName").value("Jane"));
    }

    @Test
    void getMe_withAuth_shouldReturn200() throws Exception {
        when(userService.getById(any(), any())).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane.doe@hospital.com"));
    }

    @Test
    void deactivateUser_asAdmin_shouldReturn200() throws Exception {
        doNothing().when(userService).deactivate(any(), any());

        mockMvc.perform(put("/api/v1/users/user-123/deactivate")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
