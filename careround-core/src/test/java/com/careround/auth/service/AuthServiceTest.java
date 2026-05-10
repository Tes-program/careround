package com.careround.auth.service;

import com.careround.auth.dto.ChangePasswordRequest;
import com.careround.auth.dto.JwtResponse;
import com.careround.auth.dto.LoginRequest;
import com.careround.auth.dto.RefreshTokenRequest;
import com.careround.auth.entity.RefreshToken;
import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.RefreshTokenRepository;
import com.careround.auth.repository.UserRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.auth.service.AuthServiceImpl;
import com.careround.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604_800_000L);

        testUser = new User();
        testUser.setId("user-123");
        testUser.setHospitalId("hospital-456");
        testUser.setEmail("doctor@hospital.com");
        testUser.setPasswordHash("$2a$10$encodedPassword");
        testUser.setRole(UserRole.CONSULTANT);
        testUser.setActive(true);
    }

    // ── login ──────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_shouldReturnJwtResponse() {
        LoginRequest request = new LoginRequest("hospital-456", "doctor@hospital.com", "password123");

        when(userRepository.findByHospitalIdAndEmailAndIsActiveTrue("hospital-456", "doctor@hospital.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access.token");
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900_000L);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JwtResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getHospitalId()).isEqualTo("hospital-456");
        assertThat(response.getRole()).isEqualTo("CONSULTANT");
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void login_withUnknownUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findByHospitalIdAndEmailAndIsActiveTrue(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("hospital-456", "unknown@hospital.com", "pass")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void login_withWrongPassword_shouldThrowAccessDeniedException() {
        when(userRepository.findByHospitalIdAndEmailAndIsActiveTrue(any(), any()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("wrong"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("hospital-456", "doctor@hospital.com", "wrong")))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── refresh ────────────────────────────────────────────────────────────────

    @Test
    void refresh_withValidToken_shouldRotateTokenAndReturnNewJwtResponse() {
        RefreshToken stored = buildStoredToken("valid-refresh-token", false,
                LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-refresh-token"))
                .thenReturn(Optional.of(stored));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new.access.token");
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900_000L);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JwtResponse response = authService.refresh(new RefreshTokenRequest("valid-refresh-token"));

        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
        assertThat(stored.isRevoked()).isTrue();       // old token must be revoked
        verify(refreshTokenRepository, times(1)).save(any()); // new token saved
    }

    @Test
    void refresh_withNonExistentToken_shouldThrowAccessDeniedException() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("bad-token")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void refresh_withExpiredRefreshToken_shouldRevokeAndThrowAccessDeniedException() {
        RefreshToken stored = buildStoredToken("expired-token", false,
                LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token"))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("expired-token")))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(stored.isRevoked()).isTrue();
    }

    // ── logout ─────────────────────────────────────────────────────────────────

    @Test
    void logout_withValidToken_shouldRevokeIt() {
        RefreshToken stored = buildStoredToken("valid-refresh-token", false,
                LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-refresh-token"))
                .thenReturn(Optional.of(stored));

        authService.logout("valid-refresh-token");

        assertThat(stored.isRevoked()).isTrue();
    }

    @Test
    void logout_withAlreadyRevokedToken_shouldCompleteWithoutError() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("already-revoked"))
                .thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> authService.logout("already-revoked"));
    }

    // ── changePassword ─────────────────────────────────────────────────────────

    @Test
    void changePassword_withCorrectCurrentPassword_shouldUpdateHashAndRevokeTokens() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "$2a$10$encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newHash");

        authService.changePassword("user-123", new ChangePasswordRequest("oldPass", "newPass123"));

        assertThat(testUser.getPasswordHash()).isEqualTo("$2a$10$newHash");
        verify(refreshTokenRepository).revokeAllByUserId("user-123");
    }

    @Test
    void changePassword_withWrongCurrentPassword_shouldThrowAccessDeniedException() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("wrongPass"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(
                "user-123", new ChangePasswordRequest("wrongPass", "newPass123")))
                .isInstanceOf(AccessDeniedException.class);

        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private RefreshToken buildStoredToken(String tokenValue, boolean revoked, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setUserId("user-123");
        token.setHospitalId("hospital-456");
        token.setToken(tokenValue);
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        return token;
    }
}

