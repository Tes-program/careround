package com.careround.auth.service;

import com.careround.auth.dto.ChangePasswordRequest;
import com.careround.auth.dto.JwtResponse;
import com.careround.auth.dto.LoginRequest;
import com.careround.auth.dto.RefreshTokenRequest;

public interface AuthService {

    JwtResponse login(LoginRequest request);
    JwtResponse refresh(RefreshTokenRequest refreshTokenRequest);

    void logout(String refreshToken);

    void changePassword(String userId, ChangePasswordRequest changePasswordRequest);
}
