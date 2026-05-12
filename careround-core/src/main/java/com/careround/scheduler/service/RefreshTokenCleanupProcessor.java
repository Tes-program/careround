package com.careround.scheduler.service;

import com.careround.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupProcessor {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public int deleteExpiredOrRevokedTokens() {
        return refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now(ZoneOffset.UTC));
    }
}
