package com.careround.auth.controller;

import com.careround.auth.dto.AssignWardRequest;
import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UpdateDeviceTokenRequest;
import com.careround.auth.dto.UpdateProfileRequest;
import com.careround.auth.dto.UpdateUserRequest;
import com.careround.auth.dto.UserResponse;
import com.careround.auth.service.UserService;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Tenant user administration and current-user lookup")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a tenant user", description = "Creates a user within the authenticated hospital tenant.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.create(HospitalContextHolder.getHospitalId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created successfully", user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'SUPERVISOR')")
    @Operation(summary = "List tenant users", description = "Returns all active users for the authenticated hospital tenant.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse user = userService.getById(
                HospitalContextHolder.getHospitalId(),
                HospitalContextHolder.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by id", description = "Returns a tenant user by id.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        UserResponse user = userService.getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile", description = "Allows the authenticated user to update their own first name, last name, or email.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse user = userService.updateProfile(
                HospitalContextHolder.getHospitalId(),
                HospitalContextHolder.getUserId(),
                request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Admin updates a tenant user's name, email, or role. Only provided fields are changed.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User UUID") @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(HospitalContextHolder.getHospitalId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("User updated", user));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate user", description = "Reactivates a previously deactivated tenant user account.")
    public ResponseEntity<ApiResponse<UserResponse>> reactivateUser(
            @Parameter(description = "User UUID") @PathVariable String id) {
        UserResponse user = userService.reactivate(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("User reactivated", user));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivates a tenant user account.")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        userService.deactivate(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok("User deactivated", null));
    }

    @PutMapping("/{id}/ward-assignment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign nurse to ward", description = "Assigns a user to a specific ward within the hospital tenant.")
    public ResponseEntity<ApiResponse<UserResponse>> assignWard(
            @Parameter(description = "User UUID") @PathVariable String id,
            @Valid @RequestBody AssignWardRequest request) {
        UserResponse user = userService.assignWard(
                HospitalContextHolder.getHospitalId(), id, request.wardId());
        return ResponseEntity.ok(ApiResponse.ok("Ward assigned successfully", user));
    }

    @PutMapping("/me/device-token")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update device token", description = "Registers or updates the FCM device token for the authenticated user.")
    public ResponseEntity<Void> updateDeviceToken(@Valid @RequestBody UpdateDeviceTokenRequest request) {
        userService.updateDeviceToken(
                HospitalContextHolder.getUserId(),
                HospitalContextHolder.getHospitalId(),
                request.deviceToken());
        return ResponseEntity.noContent().build();
    }
}
