package com.careround.scheduler.jobs;

import com.careround.scheduler.service.OutboxCleanupProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
@Slf4j
public class OutboxCleanupJob extends QuartzJobBean {

    private final OutboxCleanupProcessor outboxCleanupProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        int deleted = outboxCleanupProcessor.cleanup();
        log.info("action=OUTBOX_CLEANUP_JOB_COMPLETE deleted={} durationMs={}",
                deleted, System.currentTimeMillis() - startedAt);
    }
}
