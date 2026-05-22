package com.careround.scheduler.jobs;

import com.careround.scheduler.service.MedicationTaskReminderProcessor;
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
public class MedicationTaskReminderJob extends QuartzJobBean {

    private final MedicationTaskReminderProcessor medicationTaskReminderProcessor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long startedAt = System.currentTimeMillis();
        int reminders = medicationTaskReminderProcessor.processReminders();
        int overdue = medicationTaskReminderProcessor.processOverdue();
        log.info("action=REMINDER_JOB_COMPLETE reminders={} overdue={} durationMs={}",
                reminders, overdue, System.currentTimeMillis() - startedAt);
    }
}
