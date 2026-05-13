package com.careround.auth.controller;

import com.careround.auth.dto.*;
import com.careround.auth.service.AuthService;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Tenant user authentication, session refresh, logout, password changes, and account activation")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Log in a tenant user",
            description = "Authenticates an active hospital user and returns an access token plus a persisted refresh token."
    )
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh tenant JWTs",
            description = "Validates an active refresh token, revokes it, and returns a new access token and refresh token pair."
    )
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        JwtResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Log out a tenant user",
            description = "Revokes the supplied refresh token so it can no longer be used for session renewal."
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR', 'NURSE', 'WARD_SUPERVISOR')")
    @Operation(
            summary = "Change the current user's password",
            description = "Changes the authenticated user's password and revokes all of their existing refresh tokens."
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(HospitalContextHolder.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PostMapping("/activate-account")
    @Operation(
            summary = "Activate a provisioned tenant account",
            description = "Consumes a valid account activation token, sets the user's password, and enables normal login."
    )
    public ResponseEntity<ApiResponse<Void>> activateAccount(
            @Valid @RequestBody ActivateAccountRequest request) {
        authService.activateAccount(request);
        return ResponseEntity.ok(ApiResponse.ok("Account activated. Please log in.", null));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request a password reset token",
            description = "Creates a short-lived reset token for an active tenant user. The token is returned for local testing until email delivery is wired in."
    )
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("If the account exists, a reset token has been generated.", response));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password with token",
            description = "Consumes a valid password reset token, sets a new password, and revokes the user's refresh tokens."
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully", null));
    }
}
