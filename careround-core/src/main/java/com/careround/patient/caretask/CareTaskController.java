package com.careround.patient.caretask;

import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.enums.TaskStatus;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/care-tasks")
@RequiredArgsConstructor
@Tag(name = "Care Tasks", description = "Clinical and nursing care-task creation, assignment, status updates, and lookup")
public class CareTaskController {

    private final CareTaskService careTaskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'NURSE', 'WARD_SUPERVISOR')")
    @Operation(
            summary = "Create a care task",
            description = "Creates a patient care task, validates its time window, and assigns it using the active ward staffing rules."
    )
    public ResponseEntity<ApiResponse<CareTaskResponse>> createTask(@Valid @RequestBody CreateCareTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Care task created", careTaskService.createTask(request)));
    }

    @PatchMapping("/{taskId}/assign")
    @PreAuthorize("hasAnyRole('NURSE', 'WARD_SUPERVISOR')")
    @Operation(
            summary = "Assign or reassign a care task",
            description = "Assigns a care task to a nurse. Manual reassignment is restricted to ward supervisors or the task creator."
    )
    public ResponseEntity<ApiResponse<CareTaskResponse>> assignTask(
            @Parameter(description = "Care task id") @PathVariable String taskId,
            @Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Task assigned", careTaskService.assignTask(taskId, request)));
    }

    @PatchMapping("/{taskId}/progress")
    @PreAuthorize("hasAnyRole('NURSE', 'JUNIOR_DOCTOR', 'REGISTRAR', 'WARD_SUPERVISOR')")
    @Operation(
            summary = "Mark a care task in progress",
            description = "Moves a pending care task into the in-progress state."
    )
    public ResponseEntity<ApiResponse<CareTaskResponse>> progressTask(
            @Parameter(description = "Care task id") @PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.ok("Task progressed", careTaskService.progressTask(taskId)));
    }

    @PatchMapping("/{taskId}/complete")
    @PreAuthorize("hasAnyRole('NURSE', 'JUNIOR_DOCTOR', 'REGISTRAR', 'WARD_SUPERVISOR')")
    @Operation(
            summary = "Complete a care task",
            description = "Marks an in-progress or pending care task as completed."
    )
    public ResponseEntity<ApiResponse<CareTaskResponse>> completeTask(
            @Parameter(description = "Care task id") @PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.ok("Task completed", careTaskService.completeTask(taskId)));
    }

    @GetMapping("/ward/{wardId}")
    @Operation(
            summary = "List care tasks by ward",
            description = "Returns care tasks for a ward, filtered by task status. Defaults to pending tasks."
    )
    public ResponseEntity<ApiResponse<List<CareTaskResponse>>> getTasksByWard(
            @Parameter(description = "Ward id") @PathVariable String wardId,
            @Parameter(description = "Task status filter")
            @RequestParam(defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(careTaskService.getTasksByWard(wardId, status)));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(
            summary = "List care tasks by patient",
            description = "Returns all care tasks recorded for a patient."
    )
    public ResponseEntity<ApiResponse<List<CareTaskResponse>>> getTasksByPatient(
            @Parameter(description = "Patient id") @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(careTaskService.getTasksByPatient(patientId)));
    }
}
