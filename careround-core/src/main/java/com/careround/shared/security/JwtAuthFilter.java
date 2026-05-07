package com.careround.shared.security;

import com.careround.auth.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Claims claims = jwtService.validateAndParseClaims(token);

                if (jwtService.isAccessToken(claims)) {
                    String userId = claims.getSubject();
                    String hospitalId = claims.get("hospitalId", String.class);
                    String roleStr = claims.get("role", String.class);
                    UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : null;

                    HospitalContextHolder.setUserId(userId);
                    HospitalContextHolder.setHospitalId(hospitalId);
                    if (role != null) {
                        HospitalContextHolder.setRole(role);
                    }

                    MDC.put("userId", userId);
                    MDC.put("hospitalId", hospitalId);

                    var auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role.name())) : List.of()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JwtException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
        } finally {
            chain.doFilter(request, response);
            HospitalContextHolder.clear();
            MDC.clear();
        }
    }
}
