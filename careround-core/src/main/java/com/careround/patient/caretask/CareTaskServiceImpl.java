package com.careround.patient.caretask;

import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskPriority;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public CareTaskResponse createTask(CreateCareTaskRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Patient patient = patientRepository.findByIdAndHospitalId(request.patientId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (patient.getStatus() != PatientStatus.ADMITTED)
            throw new BusinessRuleException("Can only create tasks for admitted patients");

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

        CareTask saved = careTaskRepository.save(task);
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

        task.setAssignedToId(request.assignedToId());
        task.setAssignedToRole(request.assignedToRole());

        log.info("action=assignTask taskId={} assignedTo={}", taskId, request.assignedToId());
        return toResponse(careTaskRepository.save(task));
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
                t.getCompletedAt(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
