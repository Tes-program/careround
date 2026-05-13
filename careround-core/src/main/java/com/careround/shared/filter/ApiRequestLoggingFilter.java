package com.careround.shared.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_ERROR_BODY_LENGTH = 1_000;

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        Instant startedAt = Instant.now();
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        Exception failure = null;

        try {
            filterChain.doFilter(request, wrappedResponse);
        } catch (IOException | ServletException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            logRequest(request, wrappedResponse, startedAt, failure);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request,
                            ContentCachingResponseWrapper response,
                            Instant startedAt,
                            Exception failure) {
        long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
        int status = failure != null && response.getStatus() < 500 ? 500 : response.getStatus();
        String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        String message = "action=API_REQUEST method={} path={} status={} durationMs={} remoteAddr={} userId={} hospitalId={} role={}";

        Object[] args = {
                request.getMethod(),
                request.getRequestURI() + query,
                status,
                durationMs,
                request.getRemoteAddr(),
                valueOrDash(MDC.get("userId")),
                valueOrDash(MDC.get("hospitalId")),
                valueOrDash(MDC.get("role"))
        };

        if (failure != null) {
            Object[] failureArgs = append(args, failure.getClass().getSimpleName(), failure.getMessage(), failure);
            log.error(message + " exceptionClass={} exceptionMessage={}", failureArgs);
            return;
        }

        if (status >= 500) {
            log.error(message + " errorBody={}", append(args, errorBody(response)));
        } else if (status >= 400) {
            log.warn(message + " errorBody={}", append(args, errorBody(response)));
        } else {
            log.info(message, args);
        }
    }

    private String errorBody(ContentCachingResponseWrapper response) {
        byte[] body = response.getContentAsByteArray();
        if (body.length == 0) {
            return "-";
        }

        String text = new String(body, responseCharset(response))
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
        if (text.length() <= MAX_ERROR_BODY_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_ERROR_BODY_LENGTH) + "...";
    }

    private Charset responseCharset(ContentCachingResponseWrapper response) {
        if (!StringUtils.hasText(response.getCharacterEncoding())) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(response.getCharacterEncoding());
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    private String valueOrDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private Object[] append(Object[] values, Object... appended) {
        Object[] result = new Object[values.length + appended.length];
        System.arraycopy(values, 0, result, 0, values.length);
        System.arraycopy(appended, 0, result, values.length, appended.length);
        return result;
    }
}
