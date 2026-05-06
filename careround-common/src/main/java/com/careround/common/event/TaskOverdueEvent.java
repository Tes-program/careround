package com.careround.common.event;

/** Published on: TASK_OVERDUE → careround.task.overdue */
public record TaskOverdueEvent(
        String careTaskId,
        String patientId,
        String wardId,
        String assignedToId,
        String priority,
        String hospitalId
) {}
