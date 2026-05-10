package com.careround.auth.controller;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UserResponse;
import com.careround.auth.enums.UserRole;
import com.careround.auth.service.UserService;
import com.careround.shared.config.SecurityConfig;
import com.careround.shared.security.JwtService;
import lombok.AllArgsConstructor;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;

    private static final String FAKE_TOKEN = "fake.admin.token";
    private UserResponse sampleUser;

    @BeforeEach
    void setUp() {
        // Configure mock JwtService to authenticate requests with FAKE_TOKEN as ADMIN
        when(jwtService.isTokenValid(FAKE_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(FAKE_TOKEN)).thenReturn("admin-123");
        when(jwtService.extractHospitalId(FAKE_TOKEN)).thenReturn("hospital-456");
        when(jwtService.extractRole(FAKE_TOKEN)).thenReturn("ADMIN");

        sampleUser = new UserResponse(
                "user-123", "hospital-456", "Jane", "Doe",
                "jane.doe@hospital.com", UserRole.NURSE, null,
                true, LocalDateTime.now());
    }

    @Test
    void createUser_asAdmin_shouldReturn201() throws Exception {
        when(userService.create(any(), any())).thenReturn(sampleUser);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(
                                "Jane", "Doe", "jane.doe@hospital.com",
                                "password123", UserRole.NURSE, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("jane.doe@hospital.com"))
                .andExpect(jsonPath("$.data.role").value("NURSE"));
    }

    @Test
    void createUser_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(
                                "Jane", "Doe", "jane.doe@hospital.com",
                                "password123", UserRole.NURSE, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_asAdmin_shouldReturn200WithList() throws Exception {
        when(userService.listByHospital(any())).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + FAKE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].firstName").value("Jane"));
    }

    @Test
    void getMe_withAuthToken_shouldReturn200() throws Exception {
        when(userService.getById(any(), any())).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + FAKE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane.doe@hospital.com"));
    }

    @Test
    void deactivateUser_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/v1/users/user-123/deactivate")
                        .header("Authorization", "Bearer " + FAKE_TOKEN))
                .andExpect(status().isOk());
    }
}

