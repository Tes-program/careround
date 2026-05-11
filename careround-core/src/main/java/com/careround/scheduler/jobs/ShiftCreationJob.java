package com.careround.scheduler.jobs;

import com.careround.scheduler.service.ShiftCreationProcessor;
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
public class ShiftCreationJob extends QuartzJobBean {

    private final ShiftCreationProcessor shiftCreationProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            int created = shiftCreationProcessor.createShiftsForToday(correlationId);
            log.info("action=SHIFT_CREATION jobClass={} created={} durationMs={}",
                    getClass().getSimpleName(), created, System.currentTimeMillis() - startedAt);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
