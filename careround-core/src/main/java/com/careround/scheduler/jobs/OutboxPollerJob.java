package com.careround.scheduler.jobs;

import com.careround.scheduler.service.OutboxPollerProcessor;
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
public class OutboxPollerJob extends QuartzJobBean {

    private final OutboxPollerProcessor outboxPollerProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        int published = outboxPollerProcessor.pollAndPublishBatch();
        log.info("action=OUTBOX_POLL jobClass={} published={} durationMs={}",
                getClass().getSimpleName(), published, System.currentTimeMillis() - startedAt);
    }
}
