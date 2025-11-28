package io.github.honhimw.test.reactive;

import io.github.honhimw.spring.web.reactive.ExchangeContextFilter;
import io.github.honhimw.spring.web.reactive.ExchangeHolder;
import io.github.honhimw.spring.web.reactive.ReactorThreadLocalConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.handler.DefaultWebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;

/**
 * @author hon_him
 * @since 2024-07-25
 */

@Slf4j
public class ThreadLocalTests {

    @Test
    @SneakyThrows
    void api() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        ExchangeContextFilter filter = new ExchangeContextFilter("test");
        ReactorThreadLocalConfig.initialize();
        WebFilter webFilter = (exchange12, chain) -> Mono.just(exchange12)
            .doOnNext(unused -> {
                ServerWebExchange exchange1 = ExchangeHolder.getExchange();
                System.out.println(exchange1);
                assert exchange1 != null : "Thread local exchange should not be null";
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
        DefaultWebFilterChain chain = new DefaultWebFilterChain(exchange1 -> Mono.just(exchange1).then(), List.of(filter, webFilter));
        StepVerifier.create(chain.filter(exchange))
            .verifyComplete();
    }

}
