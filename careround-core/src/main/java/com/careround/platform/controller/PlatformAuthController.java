package com.careround.platform.controller;

import com.careround.platform.dto.PlatformLoginRequest;
import com.careround.platform.dto.PlatformLoginResponse;
import com.careround.platform.service.PlatformAuthService;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/auth")
@RequiredArgsConstructor
@Tag(name = "Platform Authentication", description = "Authentication for internal CareRound platform operators")
public class PlatformAuthController {

    private final PlatformAuthService platformAuthService;

    @PostMapping("/login")
    @Operation(
            summary = "Log in a platform operator",
            description = "Authenticates an internal platform operator and returns a platform-scoped JWT for onboarding administration."
    )
    public ResponseEntity<ApiResponse<PlatformLoginResponse>> login(@Valid @RequestBody PlatformLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Platform login successful", platformAuthService.login(request)));
    }
}
