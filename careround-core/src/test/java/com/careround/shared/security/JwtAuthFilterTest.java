package com.careround.shared.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;
    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        HospitalContextHolder.clear();
    }

    @Test
    void doFilterInternal_withValidBearerToken_shouldSetAuthenticationAndContext() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt.token");

        when(jwtService.isTokenValid("valid.jwt.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.jwt.token")).thenReturn("user-123");
        when(jwtService.extractHospitalId("valid.jwt.token")).thenReturn("hospital-456");
        when(jwtService.extractRole("valid.jwt.token")).thenReturn("CONSULTANT");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("user-123");
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CONSULTANT");
    }

    @Test
    void doFilterInternal_withNoAuthorizationHeader_shouldNotAuthenticate() throws Exception {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(any());
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer invalid.token");
        when(jwtService.isTokenValid("invalid.token")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_withNonBearerAuth_shouldNotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(any());
    }

    @Test
    void doFilterInternal_shouldAlwaysClearHospitalContextAfterRequest() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        when(jwtService.isTokenValid("valid.jwt.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.jwt.token")).thenReturn("user-123");
        when(jwtService.extractHospitalId("valid.jwt.token")).thenReturn("hospital-456");
        when(jwtService.extractRole("valid.jwt.token")).thenReturn("CONSULTANT");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // HospitalContextHolder must be cleared after the filter chain completes
        assertThatThrownBy(HospitalContextHolder::getHospitalId)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doFilterInternal_whenFilterChainThrows_shouldStillClearContext() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        when(jwtService.isTokenValid("valid.jwt.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.jwt.token")).thenReturn("user-123");
        when(jwtService.extractHospitalId("valid.jwt.token")).thenReturn("hospital-456");
        when(jwtService.extractRole("valid.jwt.token")).thenReturn("NURSE");
        doThrow(new RuntimeException("downstream error")).when(filterChain).doFilter(any(), any());

        assertThatThrownBy(() -> jwtAuthFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(RuntimeException.class);

        assertThatThrownBy(HospitalContextHolder::getHospitalId)
                .isInstanceOf(IllegalStateException.class);
    }
}
