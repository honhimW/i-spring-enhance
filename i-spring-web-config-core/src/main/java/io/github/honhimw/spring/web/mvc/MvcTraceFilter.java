package io.github.honhimw.spring.web.mvc;

import org.jspecify.annotations.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author hon_him
 * @since 2024-11-20
 */

public class MvcTraceFilter extends OncePerRequestFilter implements Ordered {

    public static final int DEFAULT_FILTER_ORDERED = -1000000;

    public static final String TRACE_HEADER = "request-id";
    public static final String TRACE_KEY = "traceId";

    private final String traceHeader;
    private final String traceKey;

    public static final int DEFAULT_LENGTH = 8;

    private final int length;

    public MvcTraceFilter() {
        this.traceHeader = TRACE_HEADER;
        this.traceKey = TRACE_KEY;
        this.length = DEFAULT_LENGTH;
    }

    public MvcTraceFilter(int length) {
        this.traceHeader = TRACE_HEADER;
        this.traceKey = TRACE_KEY;
        this.length = length;
    }

    public MvcTraceFilter(int length, String traceHeader, String traceKey) {
        this.traceHeader = traceHeader;
        this.traceKey = traceKey;
        this.length = length;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceId = StringUtils.getIfBlank(request.getHeader(traceHeader), () -> RandomStringUtils.secureStrong().nextAlphanumeric(length));
        MDC.put(traceKey, traceId);
        try {
            response.addHeader(traceHeader, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(traceKey);
        }

    }

    @Override
    public int getOrder() {
        return DEFAULT_FILTER_ORDERED;
    }
}
