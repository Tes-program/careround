package com.careround.auth.service;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    // 44-byte key encoded in Base64 — satisfies HS256's 256-bit minimum
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleVdoaWNoSXNMb25nRW5vdWdoRm9ySk9XVA==";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 900_000L);

        testUser = new User();
        testUser.setId("user-123");
        testUser.setHospitalId("hospital-456");
        testUser.setEmail("doctor@hospital.com");
        testUser.setRole(UserRole.CONSULTANT);
    }

    @Test
    void generateAccessToken_shouldProduceNonBlankToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(token).isNotBlank();
    }

    @Test
    void isTokenValid_withFreshToken_shouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractUserId_shouldReturnSubject() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
    }

    @Test
    void extractHospitalId_shouldReturnHospitalIdClaim() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.extractHospitalId(token)).isEqualTo("hospital-456");
    }

    @Test
    void extractRole_shouldReturnRoleName() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.extractRole(token)).isEqualTo("CONSULTANT");
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", -1_000L);
        String expiredToken = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_shouldReturnFalse() {
        String token = jwtService.generateAccessToken(testUser) + "tampered";
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_withNullToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }

    @Test
    void isTokenValid_withBlankToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid("   ")).isFalse();
    }

    @Test
    void getAccessTokenExpiryMs_shouldReturnConfiguredValue() {
        assertThat(jwtService.getAccessTokenExpiryMs()).isEqualTo(900_000L);
    }
}

