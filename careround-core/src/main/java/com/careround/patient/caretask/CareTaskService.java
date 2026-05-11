package com.careround.patient.caretask;

import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.enums.TaskStatus;

import java.util.List;

public interface CareTaskService {
    CareTaskResponse createTask(CreateCareTaskRequest request);
    CareTaskResponse assignTask(String taskId, AssignTaskRequest request);
    CareTaskResponse progressTask(String taskId);
    CareTaskResponse completeTask(String taskId);
    List<CareTaskResponse> getTasksByWard(String wardId, TaskStatus status);
    List<CareTaskResponse> getTasksByPatient(String patientId);
}
