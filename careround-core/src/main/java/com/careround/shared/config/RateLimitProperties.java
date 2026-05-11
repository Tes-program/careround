package com.careround.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "careround.ratelimit")
@Getter
@Setter
public class RateLimitProperties {

    private boolean enabled = true;
    private int limit = 200;
    private long windowSeconds = 60;

    public RateLimitProperties() {
    }

    public RateLimitProperties(boolean enabled, int limit, long windowSeconds) {
        this.enabled = enabled;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }
}
