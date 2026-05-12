package com.careround.platform.service;

import com.careround.platform.dto.PlatformLoginRequest;
import com.careround.platform.dto.PlatformLoginResponse;
import com.careround.platform.entity.PlatformOperator;
import com.careround.platform.repository.PlatformOperatorRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlatformAuthService {

    private final PlatformOperatorRepository platformOperatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public PlatformLoginResponse login(PlatformLoginRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        PlatformOperator operator = platformOperatorRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), operator.getPasswordHash())) {
            throw new AccessDeniedException("Invalid credentials");
        }
        return new PlatformLoginResponse(
                jwtService.generatePlatformAccessToken(operator),
                "Bearer",
                jwtService.getAccessTokenExpiryMs(),
                operator.getId(),
                operator.getRole().name());
    }
}
