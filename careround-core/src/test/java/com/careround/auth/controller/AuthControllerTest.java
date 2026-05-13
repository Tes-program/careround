package com.careround.auth.controller;

import com.careround.auth.dto.ChangePasswordRequest;
import com.careround.auth.dto.JwtResponse;
import com.careround.auth.dto.LoginRequest;
import com.careround.auth.dto.RefreshTokenRequest;
import com.careround.auth.service.AuthService;
import com.careround.shared.config.SecurityConfig;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;   // required by JwtAuthFilter

    private static final JwtResponse SAMPLE_JWT_RESPONSE = new JwtResponse(
            "access.token.value",
            "refresh-uuid-value",
            "Bearer",
            1_500_000L,
            "user-123",
            "hospital-456",
            "CONSULTANT"
    );

    // ── POST /api/v1/auth/login ────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_shouldReturn200WithTokens() throws Exception {
        when(authService.login(any())).thenReturn(SAMPLE_JWT_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("hospital-456", "doctor@hospital.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access.token.value"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.role").value("CONSULTANT"));
    }

    @Test
    void login_withMissingFields_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withInvalidCredentials_shouldReturn404() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResourceNotFoundException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("hospital-456", "nobody@hospital.com", "wrong"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_withWrongPassword_shouldReturn403() throws Exception {
        when(authService.login(any()))
                .thenThrow(new AccessDeniedException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("hospital-456", "doctor@hospital.com", "wrong"))))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/v1/auth/refresh ──────────────────────────────────────────────

    @Test
    void refresh_withValidToken_shouldReturn200WithNewTokens() throws Exception {
        when(authService.refresh(any())).thenReturn(SAMPLE_JWT_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshTokenRequest("valid-refresh-uuid"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access.token.value"));
    }

    @Test
    void refresh_withBlankToken_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(""))))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/v1/auth/logout ───────────────────────────────────────────────

    @Test
    void logout_withValidToken_shouldReturn200() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshTokenRequest("valid-refresh-uuid"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── POST /api/v1/auth/change-password ─────────────────────────────────────

    @Test
    void changePassword_withValidAuthToken_shouldReturn200() throws Exception {
        String fakeToken = "fake.jwt.access.token";
        when(jwtService.isTokenValid(fakeToken)).thenReturn(true);
        when(jwtService.extractUserId(fakeToken)).thenReturn("user-123");
        when(jwtService.extractHospitalId(fakeToken)).thenReturn("hospital-456");
        when(jwtService.extractRole(fakeToken)).thenReturn("CONSULTANT");
        doNothing().when(authService).changePassword(any(), any());

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + fakeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("oldPass123", "newPass456"))))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_withPlatformToken_shouldReturn403() throws Exception {
        String fakeToken = "fake.platform.jwt.access.token";
        when(jwtService.isTokenValid(fakeToken)).thenReturn(true);
        when(jwtService.extractUserId(fakeToken)).thenReturn("platform-user-123");
        when(jwtService.extractHospitalId(fakeToken)).thenReturn("PLATFORM");
        when(jwtService.extractRole(fakeToken)).thenReturn("PLATFORM_ADMIN");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + fakeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("oldPass123", "newPass456"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_withoutAuthToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("oldPass123", "newPass456"))))
                .andExpect(status().isForbidden());
    }
}

