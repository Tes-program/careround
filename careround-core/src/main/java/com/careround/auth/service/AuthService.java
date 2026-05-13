package com.careround.auth.service;

import com.careround.auth.dto.ActivateAccountRequest;
import com.careround.auth.dto.ChangePasswordRequest;
import com.careround.auth.dto.ForgotPasswordRequest;
import com.careround.auth.dto.ForgotPasswordResponse;
import com.careround.auth.dto.JwtResponse;
import com.careround.auth.dto.LoginRequest;
import com.careround.auth.dto.RefreshTokenRequest;
import com.careround.auth.dto.ResetPasswordRequest;

public interface AuthService {

    JwtResponse login(LoginRequest request);
    JwtResponse refresh(RefreshTokenRequest refreshTokenRequest);

    void logout(String refreshToken);

    void changePassword(String userId, ChangePasswordRequest changePasswordRequest);

    void activateAccount(ActivateAccountRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
