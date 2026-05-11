package com.careround.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlatformLoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
