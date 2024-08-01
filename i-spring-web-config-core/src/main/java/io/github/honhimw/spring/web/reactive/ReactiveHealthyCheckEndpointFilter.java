package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.IDataBufferUtils;
import jakarta.annotation.Nonnull;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hon_him
 * @since 2023-07-26
 */

public class ReactiveHealthyCheckEndpointFilter implements WebFilter, Ordered {

    private final List<PathPattern> _pathPatterns = new ArrayList<>();

    public ReactiveHealthyCheckEndpointFilter() {
        this("/healthy");
    }

    public ReactiveHealthyCheckEndpointFilter(String... paths) {
        for (String path : paths) {
            _pathPatterns.add(PathPatternParser.defaultInstance.parse(path));
        }
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();

        if (_pathPatterns.stream().anyMatch(pathPattern -> pathPattern.matches(path.pathWithinApplication()))) {
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(HttpStatus.OK.value());
            response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            return response.writeWith(IDataBufferUtils.wrap2Mono(response.bufferFactory(), "OK"));
        } else {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
