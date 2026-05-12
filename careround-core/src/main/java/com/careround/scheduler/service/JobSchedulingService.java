package com.careround.scheduler.service;

import com.careround.scheduler.jobs.EscalationUnacknowledgedJob;
import com.careround.scheduler.jobs.InviteExpiryJob;
import com.careround.scheduler.jobs.OutboxPollerJob;
import com.careround.scheduler.jobs.RefreshTokenCleanupJob;
import com.careround.scheduler.jobs.ShiftCreationJob;
import com.careround.scheduler.jobs.TaskOverdueJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobSchedulingService implements ApplicationListener<ContextRefreshedEvent> {

    private static final String JOB_GROUP = "careround";
    private final Scheduler scheduler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            registerSimpleJob("outboxPollerJob", OutboxPollerJob.class, 1);
            registerCronJob("shiftCreationJob", ShiftCreationJob.class, "0 * * * * ?");
            registerCronJob("taskOverdueJob", TaskOverdueJob.class, "0 */2 * * * ?");
            registerCronJob("escalationUnacknowledgedJob", EscalationUnacknowledgedJob.class, "0 */5 * * * ?");
            registerCronJob("inviteExpiryJob", InviteExpiryJob.class, "0 */30 * * * ?");
            registerCronJob("refreshTokenCleanupJob", RefreshTokenCleanupJob.class, "0 0 * * * ?");
        } catch (SchedulerException ex) {
            throw new IllegalStateException("Failed to register Quartz jobs", ex);
        }
    }

    private void registerSimpleJob(String name, Class<? extends org.quartz.Job> jobClass, int intervalSeconds)
            throws SchedulerException {
        JobKey jobKey = new JobKey(name, JOB_GROUP);
        if (scheduler.checkExists(jobKey)) {
            return;
        }

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .storeDurably()
                .build();

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey(name + "Trigger", JOB_GROUP))
                .forJob(jobDetail)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(intervalSeconds)
                        .repeatForever())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("action=QUARTZ_JOB_REGISTERED jobKey={}", jobKey);
    }

    private void registerCronJob(String name, Class<? extends org.quartz.Job> jobClass, String cron)
            throws SchedulerException {
        JobKey jobKey = new JobKey(name, JOB_GROUP);
        if (scheduler.checkExists(jobKey)) {
            return;
        }

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .storeDurably()
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey(name + "Trigger", JOB_GROUP))
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("action=QUARTZ_JOB_REGISTERED jobKey={}", jobKey);
    }
}
