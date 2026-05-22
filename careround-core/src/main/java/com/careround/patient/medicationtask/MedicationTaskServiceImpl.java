package com.careround.patient.medicationtask;

import com.careround.patient.medicationtask.dto.MedicationTaskResponse;
import com.careround.patient.medicationtask.dto.TaskListResponse;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.shared.event.MedicationTaskCompletedEvent;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationTaskServiceImpl implements MedicationTaskService {

    private static final int DUE_SOON_MINUTES = 30;

    private final MedicationTaskRepository medicationTaskRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional(readOnly = true)
    public TaskListResponse getTaskList(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        List<MedicationTask> tasks = medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(
                wardId, hospitalId, List.of(MedicationTaskStatus.PENDING, MedicationTaskStatus.OVERDUE));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime dueSoonThreshold = now.plusMinutes(DUE_SOON_MINUTES);

        List<MedicationTaskResponse> overdue = tasks.stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.OVERDUE)
                .map(this::toResponse)
                .toList();

        List<MedicationTaskResponse> dueSoon = tasks.stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.PENDING
                        && t.getScheduledTime().isBefore(dueSoonThreshold))
                .map(this::toResponse)
                .toList();

        List<MedicationTaskResponse> upcoming = tasks.stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.PENDING
                        && !t.getScheduledTime().isBefore(dueSoonThreshold))
                .map(this::toResponse)
                .toList();

        return new TaskListResponse(overdue, dueSoon, upcoming);
    }

    @Override
    @Transactional
    public void complete(String taskId, String actualDoseGiven) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        MedicationTask task = medicationTaskRepository.findByIdAndHospitalId(taskId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication task not found: " + taskId));

        if (task.getStatus() == MedicationTaskStatus.COMPLETED) {
            throw new IllegalStateException("Task is already completed: " + taskId);
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        task.setStatus(MedicationTaskStatus.COMPLETED);
        task.setCompletedAt(now);
        task.setCompletedById(userId);
        task.setActualDoseGiven(actualDoseGiven);
        medicationTaskRepository.save(task);

        outboxService.publish("medication-task-completed",
                new MedicationTaskCompletedEvent(UUID.randomUUID().toString(), taskId,
                        task.getMedicationChartId(), task.getPatientId(), task.getWardId(),
                        hospitalId, userId, actualDoseGiven, now, MDC.get("correlationId"), now),
                hospitalId);

        log.info("action=TASK_COMPLETED taskId={} completedBy={}", taskId, userId);
    }

    private MedicationTaskResponse toResponse(MedicationTask t) {
        return new MedicationTaskResponse(
                t.getId(), t.getMedicationChartId(), t.getPatientId(), t.getHospitalId(),
                t.getWardId(), t.getAssignedNurseId(), t.getScheduledTime(), t.getStatus(),
                t.getCompletedAt(), t.getCompletedById(), t.getActualDoseGiven(),
                t.getPreReminderSentAt(), t.getOverdueAlertSentAt(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
