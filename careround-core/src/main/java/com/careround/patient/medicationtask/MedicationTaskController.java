package com.careround.patient.medicationtask;

import com.careround.patient.medicationtask.dto.CompleteTaskRequest;
import com.careround.patient.medicationtask.dto.TaskListResponse;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medication-tasks")
@RequiredArgsConstructor
@Tag(name = "Medication Tasks", description = "Nurse medication task management")
public class MedicationTaskController {

    private final MedicationTaskService medicationTaskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR', 'SUPERVISOR')")
    @Operation(summary = "Get task list for a ward", description = "Returns medication tasks grouped by urgency.")
    public ResponseEntity<ApiResponse<TaskListResponse>> getTaskList(
            @Parameter(description = "Ward UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String wardId) {
        return ResponseEntity.ok(ApiResponse.ok(medicationTaskService.getTaskList(wardId)));
    }

    @PutMapping("/{taskId}/complete")
    @PreAuthorize("hasRole('NURSE')")
    @Operation(
            summary = "Mark a medication task as complete",
            description = "Marks a task as administered. Optionally include the actual dose given in the request body — omit the body entirely if the prescribed dose was given as-is."
    )
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @Parameter(description = "Medication task UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String taskId,
            @RequestBody(required = false) CompleteTaskRequest request) {
        String actualDoseGiven = request != null ? request.actualDoseGiven() : null;
        medicationTaskService.complete(taskId, actualDoseGiven);
        return ResponseEntity.ok(ApiResponse.ok("Task completed", null));
    }
}
