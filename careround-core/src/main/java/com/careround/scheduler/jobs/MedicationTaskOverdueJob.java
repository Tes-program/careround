package com.careround.scheduler.jobs;

import com.careround.scheduler.service.MedicationTaskOverdueProcessor;
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
public class MedicationTaskOverdueJob extends QuartzJobBean {

    private final MedicationTaskOverdueProcessor medicationTaskOverdueProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        int count = medicationTaskOverdueProcessor.processOverdueTasks();
        log.info("action=OVERDUE_JOB_COMPLETE count={} durationMs={}",
                count, System.currentTimeMillis() - startedAt);
    }
}
