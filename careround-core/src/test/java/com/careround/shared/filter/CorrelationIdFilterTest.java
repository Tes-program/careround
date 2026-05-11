package com.careround.shared.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void missingHeader_generatesNewCorrelationId() throws Exception {
        String[] correlationIdSeenByChain = new String[1];
        FilterChain chain = (req, res) -> correlationIdSeenByChain[0] = MDC.get("correlationId");

        filter.doFilterInternal(request, response, chain);

        assertThat(correlationIdSeenByChain[0]).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isEqualTo(correlationIdSeenByChain[0]);
    }

    @Test
    void presentHeader_usesPresentCorrelationId() throws Exception {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123");
        String[] correlationIdSeenByChain = new String[1];
        FilterChain chain = (req, res) -> correlationIdSeenByChain[0] = MDC.get("correlationId");

        filter.doFilterInternal(request, response, chain);

        assertThat(correlationIdSeenByChain[0]).isEqualTo("corr-123");
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-123");
    }

    @Test
    void mdcClearedAfterRequest() throws Exception {
        FilterChain chain = (req, res) -> assertThat(MDC.get("correlationId")).isNotBlank();

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("correlationId")).isNull();
    }
}
