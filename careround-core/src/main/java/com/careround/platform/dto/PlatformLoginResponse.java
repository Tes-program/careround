package com.careround.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlatformLoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String operatorId;
    private String role;
}
