package com.careround.auth.service;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(String name, CreateUserRequest request);

    UserResponse getById(String hospitalId, String userId);

    void deactivate(String hospitalId, String userId);

    List<UserResponse> listByHospital(String hospitalId);
}
