package com.careround.scheduler.jobs;

import com.careround.auth.repository.RefreshTokenRepository;
import com.careround.scheduler.service.RefreshTokenCleanupProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupJobTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenCleanupProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new RefreshTokenCleanupProcessor(refreshTokenRepository);
    }

    @Test
    void deleteExpiredOrRevokedTokens_deletesTokensBeforeCurrentUtcTime() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class)))
                .thenReturn(3);

        int deleted = processor.deleteExpiredOrRevokedTokens();

        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        assertThat(deleted).isEqualTo(3);
        verify(refreshTokenRepository).deleteExpiredOrRevoked(nowCaptor.capture());
        assertThat(nowCaptor.getValue()).isBeforeOrEqualTo(LocalDateTime.now(ZoneOffset.UTC));
    }
}
