package com.careround.scheduler.jobs;

import com.careround.scheduler.service.EscalationUnacknowledgedProcessor;
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
public class EscalationUnacknowledgedJob extends QuartzJobBean {

    private final EscalationUnacknowledgedProcessor escalationUnacknowledgedProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            int processed = escalationUnacknowledgedProcessor.process(correlationId);
            log.info("action=ESCALATION_UNACKNOWLEDGED_NOTIFIED jobClass={} count={} durationMs={}",
                    getClass().getSimpleName(), processed, System.currentTimeMillis() - startedAt);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
