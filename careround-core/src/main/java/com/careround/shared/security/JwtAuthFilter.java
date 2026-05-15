package com.careround.shared.security;

import com.careround.auth.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_ATTRIBUTE = "authError";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null) {
                if (jwtService.isTokenValid(token)) {
                    String userId     = jwtService.extractUserId(token);
                    String hospitalId = jwtService.extractHospitalId(token);
                    String role       = jwtService.extractRole(token);

                    var auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role)) : List.of()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);
                    setLoggingContext(hospitalId, userId, role);
                    setHospitalContextIfTenantUser(hospitalId, userId, role);
                } else {
                    request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid or expired access token");
                }
            }
        } catch (JwtException ex) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid or expired access token");
            log.debug("Invalid JWT token: {}", ex.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        }finally {
            clearLoggingContext();
            HospitalContextHolder.clear();
        }
    }

    private void setLoggingContext(String hospitalId, String userId, String role) {
        if (StringUtils.hasText(hospitalId)) {
            MDC.put("hospitalId", hospitalId);
        }
        if (StringUtils.hasText(userId)) {
            MDC.put("userId", userId);
        }
        if (StringUtils.hasText(role)) {
            MDC.put("role", role);
        }
    }

    private void clearLoggingContext() {
        MDC.remove("hospitalId");
        MDC.remove("userId");
        MDC.remove("role");
    }

    private void setHospitalContextIfTenantUser(String hospitalId, String userId, String role) {
        if (!StringUtils.hasText(role) || "PLATFORM_ADMIN".equals(role)) {
            return;
        }
        HospitalContextHolder.set(hospitalId, userId, UserRole.valueOf(role));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
