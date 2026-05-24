package com.careround.auth.dto;

import com.careround.auth.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private UserRole role;
}
