package com.careround.patient.caretask;

record CareTaskAssignmentResult(
        String nurseId,
        boolean workloadConflict,
        String workloadConflictReason
) {}
