package com.careround.auth.dto;

import com.careround.auth.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    public String accessToken;
    public String refreshToken;
    public String tokenType;
    public Long expiresIn;
    public String userId;
    public String hospitalId;
    public String role;
}
