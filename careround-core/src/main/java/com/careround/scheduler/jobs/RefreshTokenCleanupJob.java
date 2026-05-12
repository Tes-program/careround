package com.careround.scheduler.jobs;

import com.careround.scheduler.service.RefreshTokenCleanupProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.MDC;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupJob extends QuartzJobBean {

    private final RefreshTokenCleanupProcessor refreshTokenCleanupProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            int deleted = refreshTokenCleanupProcessor.deleteExpiredOrRevokedTokens();
            log.info("action=REFRESH_TOKENS_CLEANED jobClass={} count={} durationMs={}",
                    getClass().getSimpleName(), deleted, System.currentTimeMillis() - startedAt);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
