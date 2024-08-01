package io.github.honhimw.spring.web.reactive;

import jakarta.annotation.Nonnull;
import org.springframework.http.server.reactive.HttpHandler;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Auto config Thread-Local-Accessor for WebFilter, and build context-writer.
 * <pre>{@code
 * public class MyTraceFilter extends AbstractThreadLocalWebFilter<Map<String, String>> {
 *     public MyTraceFilter() {
 *         super("my-trace-id");
 *     }
 *
 *     @Override
 *     protected Supplier<Map<String, String>> doGet() {
 *         return MDC::getCopyOfContextMap;
 *     }
 *
 *     @Override
 *     protected Consumer<Map<String, String>> doSet() {
 *         return MDC::setContextMap;
 *     }
 *
 *     @Override
 *     protected Runnable doRemove() {
 *         return MDC::clear;
 *     }
 *
 *     @Override
 *     public Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response) {
 *         String traceId = "<random-id>";
 *         return httpHandler.handle(request, response)
 *                 .contextWrite(ctx -> contextWriter.apply(ctx, Map.of("traceId", traceId)));
 *     }
 * }
 * }</pre>
 * @author hon_him
 * @since 2024-07-25
 */

public abstract class AbstractThreadLocalHttpHandler<T> implements HttpHandler {

    protected final HttpHandler httpHandler;

    protected final String key;

    protected final BiFunction<Context, T, Context> contextWriter;

    public AbstractThreadLocalHttpHandler(HttpHandler httpHandler, String key) {
        this.httpHandler = httpHandler;
        this.key = key;
        this.contextWriter = ReactorThreadLocalConfig.addAccessor(key, o -> Objects.nonNull(doGet().get()), doGet(), doSet(), doRemove());
    }

    @Nonnull
    protected abstract Supplier<T> doGet();

    @Nonnull
    protected abstract Consumer<T> doSet();

    @Nonnull
    protected abstract Runnable doRemove();

}
