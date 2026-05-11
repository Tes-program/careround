package com.careround.scheduler.jobs;

import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.scheduler.service.TaskOverdueProcessor;
import com.careround.shared.event.TaskOverdueEvent;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskOverdueJobTest {

    @Mock
    private CareTaskRepository careTaskRepository;

    @Mock
    private SystemConfigurationRepository systemConfigurationRepository;

    @Mock
    private OutboxService outboxService;

    private TaskOverdueProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TaskOverdueProcessor(careTaskRepository, systemConfigurationRepository, outboxService);
    }

    @Test
    void overdueTask_setsEscalatedAtAndPublishesEvent() {
        CareTask task = overdueTask();
        SystemConfiguration config = config(30);
        when(careTaskRepository.findAllByStatusInAndEscalatedAtIsNullAndWindowEndBefore(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(task));
        when(systemConfigurationRepository.findAllByHospitalIdIn(Set.of("hosp-1")))
                .thenReturn(List.of(config));

        int processed = processor.processOverdueTasks("corr-1");

        assertThat(processed).isEqualTo(1);
        assertThat(task.getEscalatedAt()).isNotNull();
        verify(outboxService).publish(eq("TASK_OVERDUE"), any(TaskOverdueEvent.class), eq("hosp-1"));
        verify(careTaskRepository).save(task);
    }

    @Test
    void alreadyEscalated_skipped() {
        CareTask task = overdueTask();
        task.setEscalatedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        when(careTaskRepository.findAllByStatusInAndEscalatedAtIsNullAndWindowEndBefore(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(task));
        when(systemConfigurationRepository.findAllByHospitalIdIn(Set.of("hosp-1")))
                .thenReturn(List.of(config(30)));

        int processed = processor.processOverdueTasks("corr-1");

        assertThat(processed).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void gracePeriodsReadFromSystemConfiguration() {
        CareTask task = overdueTask();
        task.setWindowEnd(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(20));
        when(careTaskRepository.findAllByStatusInAndEscalatedAtIsNullAndWindowEndBefore(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(task));
        when(systemConfigurationRepository.findAllByHospitalIdIn(Set.of("hosp-1")))
                .thenReturn(List.of(config(25)));

        int processed = processor.processOverdueTasks("corr-1");

        assertThat(processed).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    private CareTask overdueTask() {
        CareTask task = new CareTask();
        task.setId("task-1");
        task.setHospitalId("hosp-1");
        task.setPatientId("patient-1");
        task.setWardId("ward-1");
        task.setAssignedToId("user-1");
        task.setTitle("Administer medication");
        task.setStatus(TaskStatus.PENDING);
        task.setWindowEnd(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(40));
        return task;
    }

    private SystemConfiguration config(int graceMinutes) {
        SystemConfiguration configuration = new SystemConfiguration();
        configuration.setHospitalId("hosp-1");
        configuration.setTaskOverdueGraceMinutes(graceMinutes);
        return configuration;
    }
}
