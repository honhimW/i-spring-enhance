package io.github.honhimw.spring.web.reactive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.util.JsonUtils;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.web.util.LoggingSystemUtils;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
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

import java.io.IOException;
import java.util.*;

/**
 * <pre>
 *     <span style="color:orange">PUT</span> <span style="color:yellow">/logging</span>
 *     <span style="color:red">content-type:</span> application/json
 *
 *     {
 *         "org.example": "debug",
 *         "java.lang": "ERROR"
 *     }
 * </pre>
 *
 * @author hon_him
 * @since 2023-07-26
 */

@Slf4j
public class ReactiveLoggingRebinderEndpointFilter implements WebFilter, Ordered {

    private final List<PathPattern> _pathPatterns = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReactiveLoggingRebinderEndpointFilter() {
        this("/logging");
    }

    public ReactiveLoggingRebinderEndpointFilter(String... paths) {
        for (String path : paths) {
            _pathPatterns.add(PathPatternParser.defaultInstance.parse(path));
        }
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        RequestPath path = request.getPath();

        if (_pathPatterns.stream().anyMatch(pathPattern -> pathPattern.matches(path.pathWithinApplication()))) {
            if (request.getMethod() == HttpMethod.PUT) {
                if (MediaType.APPLICATION_JSON.isCompatibleWith(request.getHeaders().getContentType())) {
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
                    return IDataBufferUtils.fluxData2Bytes(request.getBody())
                        .map(this::readTree)
                        .doOnNext(jsonNode -> jsonNode.fields()
                            .forEachRemaining(field -> {
                                String key = field.getKey();
                                JsonNode value = field.getValue();
                                LoggingSystemUtils.setLevel(key, value.textValue());
                            }))
                        .then(response.writeWith(IDataBufferUtils.wrap2Mono(response.bufferFactory(), "OK")));
                }
            } else if (request.getMethod() == HttpMethod.GET) {
                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                List<String> loggers = request.getQueryParams().get("logger");
                loggers = Objects.requireNonNullElseGet(loggers, ArrayList::new);
                Map<String, String> loggerLevelMap = new HashMap<>();
                loggers.forEach(logger -> {
                    LogLevel logLevel = LoggingSystemUtils.getLogLevel(logger);
                    loggerLevelMap.put(logger, logLevel.name());
                });
                return response.writeWith(IDataBufferUtils.wrap2Mono(response.bufferFactory(), JsonUtils.toJson(loggerLevelMap)));
            }
        }
        return chain.filter(exchange);


    }

    @Override
    public int getOrder() {
        return -10;
    }

    protected JsonNode readTree(byte[] bytes) {
        try {
            return objectMapper.readTree(bytes);
        } catch (IOException e) {
            return objectMapper.createObjectNode();
        }
    }

}
