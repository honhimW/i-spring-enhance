package io.github.honhimw.spring.web.reactive;

import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author hon_him
 * @since 2023-04-10
 */

public class ExchangeHolder {

    private static final ThreadLocal<ServerWebExchange> exchangeHolder = new NamedInheritableThreadLocal<>("server web exchange");

    public static void resetExchange() {
        exchangeHolder.remove();
    }

    public static void setExchange(ServerWebExchange exchange) {
        if (exchange == null) {
            resetExchange();
        } else {
            exchangeHolder.set(exchange);
        }
    }

    public static ServerWebExchange getExchange() {
        return exchangeHolder.get();
    }

}
