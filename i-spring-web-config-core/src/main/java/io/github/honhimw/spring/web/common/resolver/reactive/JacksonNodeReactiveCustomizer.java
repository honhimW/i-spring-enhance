package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

/**
 * @author hon_him
 * @since 2022-08-22
 */
@FunctionalInterface
public interface JacksonNodeReactiveCustomizer {

    /**
     * @param objectNode         data container
     * @param parameter          parameter in endpoint
     * @param serverHttpRequest  request
     */
    Mono<Void> customize(ObjectNode objectNode, MethodParameter parameter, ServerHttpRequest serverHttpRequest);

}
