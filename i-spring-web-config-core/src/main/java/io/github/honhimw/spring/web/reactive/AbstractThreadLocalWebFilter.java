package io.github.honhimw.spring.web.reactive;

import org.jspecify.annotations.NonNull;
import org.springframework.web.server.WebFilter;
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
 *     public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
 *         String traceId = "<random-id>";
 *         return chain.filter(exchange)
 *                 .contextWrite(ctx -> contextWriter.apply(ctx, Map.of("traceId", traceId)));
 *     }
 * }
 * }</pre>
 * @author hon_him
 * @since 2024-07-25
 */

public abstract class AbstractThreadLocalWebFilter<T> implements WebFilter {

    protected final String key;

    protected final BiFunction<Context, T, Context> contextWriter;

    public AbstractThreadLocalWebFilter(String key) {
        this.key = key;
        this.contextWriter = ReactorThreadLocalConfig.addAccessor(key, o -> Objects.nonNull(doGet().get()), doGet(), doSet(), doRemove());
    }

    @NonNull
    protected abstract Supplier<T> doGet();

    @NonNull
    protected abstract Consumer<T> doSet();

    @NonNull
    protected abstract Runnable doRemove();

}
