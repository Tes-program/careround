package com.careround.shared.config;

import com.careround.shared.filter.CorrelationIdFilter;
import com.careround.shared.filter.ApiRequestLoggingFilter;
import com.careround.shared.filter.RateLimitingFilter;
import com.careround.shared.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final ObjectProvider<ApiRequestLoggingFilter> apiRequestLoggingFilterProvider;
    private final ObjectProvider<RateLimitingFilter> rateLimitingFilterProvider;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HttpSecurity security = http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(this::writeUnauthorizedResponse))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui",
                                "/swagger-ui/",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/docs"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/onboarding/hospital-requests").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/platform/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/change-password").authenticated()
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);

        RateLimitingFilter rateLimitingFilter = rateLimitingFilterProvider.getIfAvailable();
        if (rateLimitingFilter != null) {
            security.addFilterAfter(rateLimitingFilter, CorrelationIdFilter.class);
            security.addFilterAfter(jwtAuthFilter, RateLimitingFilter.class);
        } else {
            security.addFilterAfter(jwtAuthFilter, CorrelationIdFilter.class);
        }
        apiRequestLoggingFilterProvider.ifAvailable(filter -> security.addFilterAfter(filter, JwtAuthFilter.class));

        return security.build();
    }

    private void writeUnauthorizedResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("WWW-Authenticate", "Bearer");

        Object authError = request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTRIBUTE);
        String message = authError instanceof String ? (String) authError : "Authentication required";
        String correlationId = (String) request.getAttribute("correlationId");
        String body = """
                {"status":401,"error":"Unauthorized","message":"%s","path":"%s","correlationId":%s,"timestamp":"%s"}""".formatted(
                escapeJson(message),
                escapeJson(request.getRequestURI()),
                correlationId == null ? "null" : "\"" + escapeJson(correlationId) + "\"",
                Instant.now()
        );
        response.getWriter().write(body);
    }

    private String escapeJson(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(CorrelationIdFilter filter) {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            ObjectProvider<RateLimitingFilter> rateLimitingFilterProvider) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        rateLimitingFilterProvider.ifAvailable(registration::setFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ApiRequestLoggingFilter> apiRequestLoggingFilterRegistration(
            ObjectProvider<ApiRequestLoggingFilter> apiRequestLoggingFilterProvider) {
        FilterRegistrationBean<ApiRequestLoggingFilter> registration = new FilterRegistrationBean<>();
        apiRequestLoggingFilterProvider.ifAvailable(registration::setFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
