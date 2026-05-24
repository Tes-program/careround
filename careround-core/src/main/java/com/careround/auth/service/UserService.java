package com.careround.auth.service;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UpdateProfileRequest;
import com.careround.auth.dto.UpdateUserRequest;
import com.careround.auth.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(String hospitalId, CreateUserRequest request);

    UserResponse getById(String hospitalId, String userId);

    UserResponse updateUser(String hospitalId, String userId, UpdateUserRequest request);

    UserResponse reactivate(String hospitalId, String userId);

    UserResponse updateProfile(String hospitalId, String userId, UpdateProfileRequest request);

    void deactivate(String hospitalId, String userId);

    List<UserResponse> listByHospital(String hospitalId);

    void updateDeviceToken(String userId, String hospitalId, String deviceToken);

    UserResponse assignWard(String hospitalId, String userId, String wardId);
}
