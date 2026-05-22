package com.careround.scheduler.service;

import com.careround.shared.event.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxCleanupProcessor {

    private static final int RETENTION_DAYS = 7;

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public int cleanup() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusDays(RETENTION_DAYS);
        int deleted = outboxEventRepository.deleteByPublishedTrueAndPublishedAtBefore(cutoff);
        log.info("action=OUTBOX_CLEANUP deleted={} cutoff={}", deleted, cutoff);
        return deleted;
    }
}
