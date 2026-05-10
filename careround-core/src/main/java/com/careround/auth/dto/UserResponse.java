package com.careround.auth.dto;

import com.careround.auth.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String hospitalId;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private String departmentId;
    private boolean isActive;
    private LocalDateTime createdAt;
}
