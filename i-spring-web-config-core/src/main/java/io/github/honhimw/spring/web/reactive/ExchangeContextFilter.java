package io.github.honhimw.spring.web.reactive;

import org.springframework.core.Ordered;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <h2>See: {@link ServerWebExchangeContextFilter#getExchange(ContextView)}</h2>
 * <pre>{@code
 * Mono<ServerWebExchange> getExchange() {
 *     return Mono.deferContextual(contextView ->
 *         Mono.justOrEmpty(ServerWebExchangeContextFilter
 *             .getExchange(contextView)));
 * }
 * }</pre>
 *
 * @author hon_him
 * @see ServerWebExchangeContextFilter reactive exchange context
 * @since 2023-04-06
 */

public class ExchangeContextFilter extends AbstractThreadLocalWebFilter<ServerWebExchange> implements WebFilter, Ordered {

    public ExchangeContextFilter(String key) {
        super(key);
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        return chain.filter(exchange)
            .contextWrite(context -> contextWriter.apply(context, exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Nonnull
    @Override
    protected Supplier<ServerWebExchange> doGet() {
        return ExchangeHolder::getExchange;
    }

    @Nonnull
    @Override
    protected Consumer<ServerWebExchange> doSet() {
        return ExchangeHolder::setExchange;
    }

    @Nonnull
    @Override
    protected Runnable doRemove() {
        return ExchangeHolder::resetExchange;
    }
}
