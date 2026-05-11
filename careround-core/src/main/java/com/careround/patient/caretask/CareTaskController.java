package com.careround.patient.caretask;

import com.careround.patient.caretask.dto.AssignTaskRequest;
import com.careround.patient.caretask.dto.CareTaskResponse;
import com.careround.patient.caretask.dto.CreateCareTaskRequest;
import com.careround.patient.enums.TaskStatus;
import com.careround.shared.dto.ApiResponse;
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
public class CareTaskController {

    private final CareTaskService careTaskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'NURSE', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<CareTaskResponse>> createTask(@Valid @RequestBody CreateCareTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Care task created", careTaskService.createTask(request)));
    }

    @PatchMapping("/{taskId}/assign")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'NURSE', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<CareTaskResponse>> assignTask(
            @PathVariable String taskId,
            @Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Task assigned", careTaskService.assignTask(taskId, request)));
    }

    @PatchMapping("/{taskId}/progress")
    @PreAuthorize("hasAnyRole('NURSE', 'JUNIOR_DOCTOR', 'REGISTRAR', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<CareTaskResponse>> progressTask(@PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.ok("Task progressed", careTaskService.progressTask(taskId)));
    }

    @PatchMapping("/{taskId}/complete")
    @PreAuthorize("hasAnyRole('NURSE', 'JUNIOR_DOCTOR', 'REGISTRAR', 'WARD_SUPERVISOR')")
    public ResponseEntity<ApiResponse<CareTaskResponse>> completeTask(@PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.ok("Task completed", careTaskService.completeTask(taskId)));
    }

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<ApiResponse<List<CareTaskResponse>>> getTasksByWard(
            @PathVariable String wardId,
            @RequestParam(defaultValue = "PENDING") TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(careTaskService.getTasksByWard(wardId, status)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<CareTaskResponse>>> getTasksByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(careTaskService.getTasksByPatient(patientId)));
    }
}
