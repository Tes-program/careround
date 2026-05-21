package com.careround.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("careround.ai")
public record AiProperties(
        String serviceUrl,
        int connectTimeoutSeconds,
        int readTimeoutSeconds
) {}
