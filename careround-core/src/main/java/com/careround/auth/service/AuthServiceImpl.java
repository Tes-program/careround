package com.careround.auth.service;

import com.careround.auth.dto.ActivateAccountRequest;
import com.careround.auth.dto.ChangePasswordRequest;
import com.careround.auth.dto.JwtResponse;
import com.careround.auth.dto.LoginRequest;
import com.careround.auth.dto.RefreshTokenRequest;
import com.careround.auth.entity.RefreshToken;
import com.careround.auth.entity.User;
import com.careround.auth.repository.RefreshTokenRepository;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Hospital;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.onboarding.entity.ActivationToken;
import com.careround.onboarding.repository.ActivationTokenRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BadCredentialsException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final HospitalRepository hospitalRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {
        Hospital hospital = hospitalRepository
                .findByCodeIgnoreCaseAndIsActiveTrue(request.getHospitalCode())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        User user = userRepository
                .findByHospitalIdAndEmailAndIsActiveTrue(hospital.getId(), request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
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
        refreshTokenRepository.revokeAllByUserId(userId);
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

    @Override
    @Transactional
    public void activateAccount(ActivateAccountRequest request) {
        String tokenHash = sha256(request.token());
        ActivationToken activationToken = activationTokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired activation token"));

        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessRuleException("Activation token has expired.");
        }

        User user = userRepository.findById(activationToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        activationToken.setUsed(true);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
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
}
