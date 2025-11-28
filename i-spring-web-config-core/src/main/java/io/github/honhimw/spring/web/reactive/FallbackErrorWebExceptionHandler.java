package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.web.common.AbstractFallbackHandler;
import io.github.honhimw.spring.web.common.ExceptionWrapper;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import org.jspecify.annotations.NonNull;
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

    private final HttpMessageEncoder<Object> httpMessageEncoder;

    public FallbackErrorWebExceptionHandler(HttpMessageEncoder<Object> httpMessageEncoder,
                                            ExceptionWrappers exceptionWrappers,
                                            ExceptionWrapper.MessageFormatter messageFormatter) {
        super(exceptionWrappers, messageFormatter);
        this.httpMessageEncoder = httpMessageEncoder;
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        log(ex);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ExceptionWrappers.Pair pair = exceptionWrappers.getWrapper(ex);
        Throwable t = pair.t();
        ExceptionWrapper wrapper = pair.ew();
        int status = wrapper.httpCode(t);
        response.setRawStatusCode(status);
        Object fail = handle(wrapper, t, status);
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = httpMessageEncoder.encodeValue(fail, dataBufferFactory, ResolvableType.forType(fail.getClass()), MediaType.APPLICATION_JSON, null);
        return response
            .writeWith(Mono.just(dataBuffer))
            .then(Mono.defer(response::setComplete))
            ;
    }

    /**
     * @see WebFluxConfigurationSupport#responseStatusExceptionHandler() @Order(0), has to work before it
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
