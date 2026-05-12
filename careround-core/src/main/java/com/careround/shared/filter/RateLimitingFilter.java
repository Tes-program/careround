package com.careround.shared.filter;

import com.careround.shared.config.RateLimitProperties;
import com.careround.shared.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;

@Component
@ConditionalOnBean(RedisTemplate.class)
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final RateLimitProperties rateLimitProperties;

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.equals("/docs")
                || path.equals("/api/v1/auth")
                || path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String identifier = resolveIdentifier(request);
        String key = "rate_limit:" + identifier;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - Duration.ofSeconds(rateLimitProperties.getWindowSeconds()).toMillis();

        var zSetOps = redisTemplate.opsForZSet();
        zSetOps.removeRangeByScore(key, 0, windowStart);
        Long count = zSetOps.zCard(key);

        if (count != null && count >= rateLimitProperties.getLimit()) {
            writeTooManyRequests(request, response);
            return;
        }

        zSetOps.add(key, String.valueOf(now), now);
        redisTemplate.expire(key, Duration.ofSeconds(rateLimitProperties.getWindowSeconds()));
        filterChain.doFilter(request, response);
    }

    private String resolveIdentifier(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            try {
                String hospitalId = jwtService.extractHospitalId(token);
                if (StringUtils.hasText(hospitalId)) {
                    return hospitalId;
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // Fall back to IP-based throttling for invalid JWTs.
            }
        }

        return StringUtils.hasText(request.getRemoteAddr()) ? request.getRemoteAddr() : "unknown";
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("status", 429);
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded");
        body.put("path", request.getRequestURI());
        body.put("correlationId", MDC.get("correlationId"));
        body.put("timestamp", Instant.now().toString());

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
