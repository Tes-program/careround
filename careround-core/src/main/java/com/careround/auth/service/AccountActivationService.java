package com.careround.auth.service;

import com.careround.auth.entity.AccountActivationToken;
import com.careround.auth.entity.User;
import com.careround.auth.repository.AccountActivationTokenRepository;
import com.careround.auth.repository.UserRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountActivationTokenRepository accountActivationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createToken(User user, int expiryHours) {
        String token = randomToken();
        AccountActivationToken activationToken = new AccountActivationToken();
        activationToken.setTokenHash(hash(token));
        activationToken.setUserId(user.getId());
        activationToken.setHospitalId(user.getHospitalId());
        activationToken.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(expiryHours));
        accountActivationTokenRepository.save(activationToken);
        return token;
    }

    @Transactional
    public void activate(String token, String password) {
        AccountActivationToken activationToken = accountActivationTokenRepository
                .findByTokenHashAndUsedAtIsNull(hash(token.trim()))
                .orElseThrow(() -> new AccessDeniedException("Invalid or used activation token"));
        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new AccessDeniedException("Activation token has expired");
        }

        User user = userRepository.findByIdAndHospitalId(
                        activationToken.getUserId(), activationToken.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);
        activationToken.setUsedAt(LocalDateTime.now(ZoneOffset.UTC));
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
