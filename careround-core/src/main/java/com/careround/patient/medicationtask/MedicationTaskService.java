package com.careround.patient.medicationtask;

import com.careround.patient.medicationtask.dto.TaskListResponse;

public interface MedicationTaskService {
    TaskListResponse getTaskList(String wardId);
    void complete(String taskId);
}
