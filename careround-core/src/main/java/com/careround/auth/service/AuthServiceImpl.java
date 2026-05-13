package com.careround.auth.service;

import com.careround.auth.dto.ChangePasswordRequest;
import com.careround.auth.dto.ForgotPasswordRequest;
import com.careround.auth.dto.ForgotPasswordResponse;
import com.careround.auth.dto.JwtResponse;
import com.careround.auth.dto.LoginRequest;
import com.careround.auth.dto.RefreshTokenRequest;
import com.careround.auth.dto.ActivateAccountRequest;
import com.careround.auth.dto.ResetPasswordRequest;
import com.careround.auth.entity.PasswordResetToken;
import com.careround.auth.entity.RefreshToken;
import com.careround.auth.entity.User;
import com.careround.auth.repository.PasswordResetTokenRepository;
import com.careround.auth.repository.RefreshTokenRepository;
import com.careround.auth.repository.UserRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccountActivationService accountActivationService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {
        User user = userRepository
                .findByHospitalIdAndEmailAndIsActiveTrue(request.getHospitalId(), request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AccessDeniedException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createAndPersistRefreshToken(user);
        return toJwtResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public JwtResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new AccessDeniedException("Invalid or expired refresh token"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            stored.setRevoked(true);
            throw new AccessDeniedException("Refresh token has expired. Please log in again.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Rotate: revoke old token, issue new one
        stored.setRevoked(true);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createAndPersistRefreshToken(user);
        return toJwtResponse(newAccessToken, newRefreshToken, user);

    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(t -> t.setRevoked(true));
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        // Invalidate all existing sessions for this user
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void activateAccount(ActivateAccountRequest request) {
        accountActivationService.activate(request.getToken(), request.getPassword());
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByHospitalIdAndEmailAndIsActiveTrue(request.hospitalId(), request.email())
                .map(user -> {
                    String token = randomToken();
                    LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30);

                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setHospitalId(user.getHospitalId());
                    resetToken.setUserId(user.getId());
                    resetToken.setTokenHash(hash(token));
                    resetToken.setExpiresAt(expiresAt);
                    passwordResetTokenRepository.save(resetToken);

                    return new ForgotPasswordResponse(token, expiresAt);
                })
                .orElseGet(() -> new ForgotPasswordResponse(null, null));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(hash(request.token()))
                .orElseThrow(() -> new AccessDeniedException("Invalid or expired password reset token"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (token.getExpiresAt().isBefore(now)) {
            throw new AccessDeniedException("Invalid or expired password reset token");
        }

        User user = userRepository.findByIdAndHospitalId(token.getUserId(), token.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        token.setUsedAt(now);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    private JwtResponse toJwtResponse(String accessToken, String refreshToken, User user) {
        return new JwtResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiryMs(),
                user.getId(),
                user.getHospitalId(),
                user.getRole().name()
        );
    }

    private String createAndPersistRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setHospitalId(user.getHospitalId());
        token.setToken(tokenValue);
        token.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC)
                .plus(Duration.ofMillis(refreshTokenExpiryMs)));
        refreshTokenRepository.save(token);
        return tokenValue;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available", ex);
        }
    }
}
