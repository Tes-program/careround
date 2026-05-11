package com.careround.scheduler.service;

import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.shared.event.TaskOverdueEvent;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskOverdueProcessor {

    private static final int DEFAULT_GRACE_MINUTES = 30;

    private final CareTaskRepository careTaskRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final OutboxService outboxService;

    @Transactional
    public int processOverdueTasks(String correlationId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<CareTask> candidates = careTaskRepository.findAllByStatusInAndEscalatedAtIsNullAndWindowEndBefore(
                List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS),
                now
        );

        if (candidates.isEmpty()) {
            return 0;
        }

        Set<String> hospitalIds = candidates.stream()
                .map(CareTask::getHospitalId)
                .collect(Collectors.toSet());
        Map<String, SystemConfiguration> configByHospital = systemConfigurationRepository.findAllByHospitalIdIn(hospitalIds)
                .stream()
                .collect(Collectors.toMap(SystemConfiguration::getHospitalId, Function.identity()));

        int processed = 0;
        for (CareTask task : candidates) {
            if (task.getEscalatedAt() != null) {
                continue;
            }

            int graceMinutes = configByHospital.getOrDefault(task.getHospitalId(), defaultConfiguration())
                    .getTaskOverdueGraceMinutes();
            if (graceMinutes <= 0) {
                graceMinutes = DEFAULT_GRACE_MINUTES;
            }

            if (task.getWindowEnd() == null || !task.getWindowEnd().isBefore(now.minusMinutes(graceMinutes))) {
                continue;
            }

            markTaskOverdue(task, correlationId);
            processed++;
        }

        return processed;
    }

    private void markTaskOverdue(CareTask task, String correlationId) {
        task.setEscalatedAt(LocalDateTime.now(ZoneOffset.UTC));
        outboxService.publish(
                "TASK_OVERDUE",
                new TaskOverdueEvent(
                        task.getHospitalId(),
                        task.getId(),
                        task.getPatientId(),
                        task.getWardId(),
                        task.getAssignedToId(),
                        task.getTitle(),
                        task.getWindowEnd() != null ? task.getWindowEnd().toString() : null,
                        correlationId
                ),
                task.getHospitalId()
        );
        careTaskRepository.save(task);
    }

    private SystemConfiguration defaultConfiguration() {
        SystemConfiguration configuration = new SystemConfiguration();
        configuration.setTaskOverdueGraceMinutes(DEFAULT_GRACE_MINUTES);
        return configuration;
    }
}
