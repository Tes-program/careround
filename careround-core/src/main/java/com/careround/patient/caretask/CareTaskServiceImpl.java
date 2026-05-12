package com.careround.patient.caretask;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.AssignedToRole;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.CareTaskWorkloadConflictEvent;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CareTaskServiceImpl implements CareTaskService {

    private final CareTaskRepository careTaskRepository;
    private final PatientRepository patientRepository;
    private final WardRepository wardRepository;
    private final CareTaskAssignmentService careTaskAssignmentService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public CareTaskResponse createTask(CreateCareTaskRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Patient patient = patientRepository.findByIdAndHospitalId(request.patientId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (patient.getStatus() != PatientStatus.ADMITTED)
            throw new BusinessRuleException("Can only create tasks for admitted patients");
        validateTaskWindow(request.windowStart(), request.windowEnd());

        CareTask task = new CareTask();
        task.setHospitalId(hospitalId);
        task.setPatientId(request.patientId());
        task.setWardId(patient.getWardId());
        task.setRoundId(request.roundId());
        task.setCreatedById(userId);
        task.setTaskType(request.taskType());
        task.setSource(request.source());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.ROUTINE);
        task.setWindowStart(request.windowStart());
        task.setWindowEnd(request.windowEnd());
        task.setAssignedToRole(AssignedToRole.NURSE);

        CareTaskAssignmentResult assignment = careTaskAssignmentService.assignForNewTask(
                hospitalId,
                patient.getWardId(),
                request.windowStart(),
                request.windowEnd());
        task.setAssignedToId(assignment.nurseId());
        task.setWorkloadConflict(assignment.workloadConflict());
        task.setWorkloadConflictReason(assignment.workloadConflictReason());

        CareTask saved = careTaskRepository.save(task);
        publishWorkloadConflictIfNeeded(saved);
        log.info("action=createTask taskId={} patientId={} hospitalId={}", saved.getId(), request.patientId(), hospitalId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CareTaskResponse assignTask(String taskId, AssignTaskRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        CareTask task = careTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Care task not found"));
        if (!task.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Task does not belong to this hospital");
        if (task.getStatus() != TaskStatus.PENDING)
            throw new BusinessRuleException("Only PENDING tasks can be assigned");
        validateManualAssignmentPermission(task);
        validateTaskWindow(task.getWindowStart(), task.getWindowEnd());

        task.setAssignedToId(request.assignedToId());
        task.setAssignedToRole(request.assignedToRole());
        if (request.assignedToRole() == AssignedToRole.NURSE
                && careTaskAssignmentService.hasConflictExcludingTask(
                hospitalId, request.assignedToId(), task.getId(), task.getWindowStart(), task.getWindowEnd())) {
            task.setWorkloadConflict(true);
            task.setWorkloadConflictReason("Manual override assigned this task despite an overlapping care task.");
        } else {
            task.setWorkloadConflict(false);
            task.setWorkloadConflictReason(null);
        }

        log.info("action=assignTask taskId={} assignedTo={}", taskId, request.assignedToId());
        CareTask saved = careTaskRepository.save(task);
        publishWorkloadConflictIfNeeded(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CareTaskResponse progressTask(String taskId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        CareTask task = careTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Care task not found"));
        if (!task.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Task does not belong to this hospital");
        if (task.getStatus() != TaskStatus.PENDING)
            throw new BusinessRuleException("Only PENDING tasks can be progressed to IN_PROGRESS");

        task.setStatus(TaskStatus.IN_PROGRESS);

        log.info("action=progressTask taskId={} hospitalId={}", taskId, hospitalId);
        return toResponse(careTaskRepository.save(task));
    }

    @Override
    @Transactional
    public CareTaskResponse completeTask(String taskId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        CareTask task = careTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Care task not found"));
        if (!task.getHospitalId().equals(hospitalId))
            throw new AccessDeniedException("Task does not belong to this hospital");
        if (task.getStatus() != TaskStatus.IN_PROGRESS)
            throw new BusinessRuleException("Only IN_PROGRESS tasks can be completed");

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedById(userId);
        task.setCompletedAt(LocalDateTime.now(ZoneOffset.UTC));

        log.info("action=completeTask taskId={} completedBy={}", taskId, userId);
        return toResponse(careTaskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareTaskResponse> getTasksByWard(String wardId, TaskStatus status) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        return careTaskRepository.findAllByHospitalIdAndWardIdAndStatusOrderByWindowEnd(hospitalId, wardId, status)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareTaskResponse> getTasksByPatient(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return careTaskRepository.findAllByPatientId(patientId)
                .stream().map(this::toResponse).toList();
    }

    private CareTaskResponse toResponse(CareTask t) {
        return new CareTaskResponse(t.getId(), t.getHospitalId(), t.getPatientId(), t.getWardId(),
                t.getRoundId(), t.getCreatedById(), t.getAssignedToId(), t.getAssignedToRole(),
                t.getTaskType(), t.getSource(), t.getTitle(), t.getDescription(), t.getPriority(),
                t.getWindowStart(), t.getWindowEnd(), t.getStatus(), t.getCompletedById(),
                t.getCompletedAt(), t.isWorkloadConflict(), t.getWorkloadConflictReason(),
                t.getCreatedAt(), t.getUpdatedAt());
    }

    private void validateTaskWindow(LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (windowStart == null || windowEnd == null) {
            throw new BusinessRuleException("All care tasks must have a time window");
        }
        if (!windowEnd.isAfter(windowStart)) {
            throw new BusinessRuleException("Care task windowEnd must be after windowStart");
        }
    }

    private void validateManualAssignmentPermission(CareTask task) {
        UserRole role = HospitalContextHolder.getRole();
        String userId = HospitalContextHolder.getUserId();
        if (role == UserRole.WARD_SUPERVISOR) {
            return;
        }
        if (role == UserRole.NURSE && task.getCreatedById().equals(userId)) {
            return;
        }
        throw new AccessDeniedException("Only a ward supervisor or the nurse who created the task can reassign it");
    }

    private void publishWorkloadConflictIfNeeded(CareTask task) {
        if (!task.isWorkloadConflict()) {
            return;
        }
        Ward ward = wardRepository.findByIdAndHospitalId(task.getWardId(), task.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        outboxService.publish("careround.care_task.workload_conflict",
                new CareTaskWorkloadConflictEvent(
                        task.getHospitalId(),
                        task.getId(),
                        task.getWardId(),
                        task.getPatientId(),
                        task.getAssignedToId(),
                        ward.getSupervisorId(),
                        task.getWindowStart(),
                        task.getWindowEnd(),
                        task.getWorkloadConflictReason(),
                        MDC.get("correlationId")
                ),
                task.getHospitalId());
    }
}
