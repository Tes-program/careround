package com.careround.shared.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRequestLoggingFilterTest {

    private ApiRequestLoggingFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        filter = new ApiRequestLoggingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        logger = (Logger) LoggerFactory.getLogger(ApiRequestLoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        MDC.clear();
    }

    @Test
    void shouldOnlyLogApiRequests() {
        request.setRequestURI("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();

        request.setRequestURI("/api/v1/users/me");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void errorResponse_shouldBeLoggedAndReturnedToClient() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/v1/users/me");
        request.setRemoteAddr("127.0.0.1");
        MDC.put("userId", "user-123");
        MDC.put("hospitalId", "hospital-456");
        MDC.put("role", "ADMIN");

        FilterChain chain = (req, res) -> {
            HttpServletResponse servletResponse = (HttpServletResponse) res;
            servletResponse.setStatus(422);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"message\":\"Validation failed\"}");
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"Validation failed\"}");
        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("action=API_REQUEST")
                .contains("method=GET")
                .contains("path=/api/v1/users/me")
                .contains("status=422")
                .contains("userId=user-123")
                .contains("hospitalId=hospital-456")
                .contains("role=ADMIN")
                .contains("errorBody={\"message\":\"Validation failed\"}");
    }
}
