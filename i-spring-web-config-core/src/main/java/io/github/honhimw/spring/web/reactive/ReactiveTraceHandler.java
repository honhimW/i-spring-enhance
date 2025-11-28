package io.github.honhimw.spring.web.reactive;

import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author hon_him
 * @since 2024-11-11
 */

@Slf4j
public class ReactiveTraceHandler extends AbstractThreadLocalHttpHandler<Map<String, String>> implements HttpHandler {

    public static final int DEFAULT_HANDLER_ORDERED = -1000000;
    public static final String REACTOR_MDC_KEY = "MDC_TLA";
    // Since RFC 6648, starts with 'x-' is not recommended.
    public static final String TRACE_HEADER = "request-id";
    public static final String TRACE_KEY = "traceId";

    private final HttpHandler httpHandler;
    private final String traceHeader;
    private final String traceKey;

    public static final int DEFAULT_LENGTH = 8;

    private final int length;

    public ReactiveTraceHandler(HttpHandler httpHandler) {
        this(httpHandler, DEFAULT_LENGTH);
    }

    public ReactiveTraceHandler(HttpHandler httpHandler, int length) {
        super(httpHandler, REACTOR_MDC_KEY);
        this.httpHandler = httpHandler;
        this.length = length > 0 ? length : DEFAULT_LENGTH;
        this.traceHeader = TRACE_HEADER;
        this.traceKey = TRACE_KEY;
    }

    public ReactiveTraceHandler(HttpHandler httpHandler, String key, int length, String traceHeader, String traceKey) {
        super(httpHandler, key);
        this.httpHandler = httpHandler;
        this.length = length;
        this.traceHeader = traceHeader;
        this.traceKey = traceKey;
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        String traceId = StringUtils.getIfBlank(request.getHeaders().getFirst(traceHeader), () -> RandomStringUtils.secureStrong().nextAlphanumeric(length));
        final ServerHttpRequest _request = request.mutate().headers(httpHeaders -> httpHeaders.add(traceHeader, traceId)).build();
        response.getHeaders().add(traceHeader, traceId);
        return httpHandler.handle(_request, response)
            .contextWrite(context -> contextWriter.apply(context, Map.of(traceKey, traceId)));
    }

    @NonNull
    @Override
    protected Supplier<Map<String, String>> doGet() {
        return MDC::getCopyOfContextMap;
    }

    @NonNull
    @Override
    protected Consumer<Map<String, String>> doSet() {
        return MDC::setContextMap;
    }

    @NonNull
    @Override
    protected Runnable doRemove() {
        return MDC::clear;
    }

}
