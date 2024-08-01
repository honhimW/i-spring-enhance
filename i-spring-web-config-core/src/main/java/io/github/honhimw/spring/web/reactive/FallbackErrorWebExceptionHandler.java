package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.web.common.AbstractFallbackHandler;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@Slf4j
public class FallbackErrorWebExceptionHandler extends AbstractFallbackHandler implements ErrorWebExceptionHandler, Ordered {

    private final ResolvableType RETURN_TYPE = ResolvableType.forType(Result.class);

    private final HttpMessageEncoder<Object> httpMessageEncoder;

    private final ExceptionWrappers exceptionWrappers;

    public FallbackErrorWebExceptionHandler(HttpMessageEncoder<Object> httpMessageEncoder, ExceptionWrappers exceptionWrappers) {
        this.httpMessageEncoder = httpMessageEncoder;
        this.exceptionWrappers = exceptionWrappers;
    }

    @Nonnull
    @Override
    public Mono<Void> handle(@Nonnull ServerWebExchange exchange, @Nonnull Throwable ex) {
        log(ex);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Object fail = exceptionWrappers.handle(ex, (exceptionWrapper, throwable) -> {
            response.setRawStatusCode(exceptionWrapper.httpCode(throwable));
            return exceptionWrapper.wrap(throwable);
        });
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = httpMessageEncoder.encodeValue(fail, dataBufferFactory, RETURN_TYPE, MediaType.APPLICATION_JSON, null);
        return response
            .writeWith(Mono.just(dataBuffer))
            .then(Mono.defer(response::setComplete))
            ;
    }

    /**
     * @see WebFluxConfigurationSupport#responseStatusExceptionHandler() @Order(0), 要在它之前执行异常处理
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
