package com.careround.scheduler.jobs;

import com.careround.scheduler.service.InviteExpiryProcessor;
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
public class InviteExpiryJob extends QuartzJobBean {

    private final InviteExpiryProcessor inviteExpiryProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            int processed = inviteExpiryProcessor.expirePendingInvites(correlationId);
            log.info("action=INVITE_EXPIRED jobClass={} count={} durationMs={}",
                    getClass().getSimpleName(), processed, System.currentTimeMillis() - startedAt);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
