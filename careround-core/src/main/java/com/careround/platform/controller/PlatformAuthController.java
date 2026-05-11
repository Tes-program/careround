package com.careround.platform.controller;

import com.careround.platform.dto.PlatformLoginRequest;
import com.careround.platform.dto.PlatformLoginResponse;
import com.careround.platform.service.PlatformAuthService;
import com.careround.shared.dto.ApiResponse;
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
public class PlatformAuthController {

    private final PlatformAuthService platformAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<PlatformLoginResponse>> login(@Valid @RequestBody PlatformLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Platform login successful", platformAuthService.login(request)));
    }
}
