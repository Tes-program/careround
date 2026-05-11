package com.careround.shared.filter;

import com.careround.shared.config.RateLimitProperties;
import com.careround.shared.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private JwtService jwtService;

    private RateLimitingFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        filter = new RateLimitingFilter(
                redisTemplate,
                jwtService,
                new ObjectMapper(),
                new RateLimitProperties(true, 2, 60)
        );
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.put("correlationId", "corr-123");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void underLimit_requestProceeds() throws Exception {
        request.setRequestURI("/api/v1/patients");
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.extractHospitalId("token")).thenReturn("hospital-1");
        when(zSetOperations.zCard("rate_limit:hospital-1")).thenReturn(1L);

        boolean[] proceeded = {false};
        FilterChain chain = (req, res) -> proceeded[0] = true;

        filter.doFilterInternal(request, response, chain);

        assertThat(proceeded[0]).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(zSetOperations).removeRangeByScore(anyString(), anyDouble(), anyDouble());
        verify(zSetOperations).add(anyString(), anyString(), anyDouble());
        verify(redisTemplate).expire(anyString(), any());
    }

    @Test
    void overLimit_returns429() throws Exception {
        request.setRequestURI("/api/v1/patients");
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.extractHospitalId("token")).thenReturn("hospital-1");
        when(zSetOperations.zCard("rate_limit:hospital-1")).thenReturn(2L);

        boolean[] proceeded = {false};
        FilterChain chain = (req, res) -> proceeded[0] = true;

        filter.doFilterInternal(request, response, chain);

        assertThat(proceeded[0]).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString())
                .contains("\"status\":429")
                .contains("\"error\":\"Too Many Requests\"")
                .contains("\"message\":\"Rate limit exceeded\"")
                .contains("\"correlationId\":\"corr-123\"");
    }

    @Test
    void authEndpoint_skipsRateLimiting() throws Exception {
        request.setRequestURI("/api/v1/auth/login");

        boolean[] proceeded = {false};
        FilterChain chain = (req, res) -> proceeded[0] = true;

        filter.doFilterInternal(request, response, chain);

        assertThat(proceeded[0]).isTrue();
        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    void noJwt_ratesLimitsByIp() throws Exception {
        request.setRequestURI("/api/v1/patients");
        request.setRemoteAddr("10.0.0.8");
        when(zSetOperations.zCard("rate_limit:10.0.0.8")).thenReturn(0L);

        FilterChain chain = (req, res) -> { };

        filter.doFilterInternal(request, response, chain);

        verify(zSetOperations).zCard("rate_limit:10.0.0.8");
    }

    @Test
    void invalidJwt_ratesLimitsByIp() throws Exception {
        request.setRequestURI("/api/v1/patients");
        request.setRemoteAddr("10.0.0.9");
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.extractHospitalId("bad-token")).thenThrow(new JwtException("bad token"));
        when(zSetOperations.zCard("rate_limit:10.0.0.9")).thenReturn(0L);

        FilterChain chain = (req, res) -> { };

        filter.doFilterInternal(request, response, chain);

        verify(zSetOperations).zCard("rate_limit:10.0.0.9");
    }
}
