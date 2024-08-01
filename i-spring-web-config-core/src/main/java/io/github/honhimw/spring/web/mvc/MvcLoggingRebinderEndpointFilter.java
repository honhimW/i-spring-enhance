package io.github.honhimw.spring.web.mvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.util.JsonUtils;
import io.github.honhimw.spring.web.util.LoggingSystemUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.*;

/**
 * 日志级别
 * @author hon_him
 * @since 2023-05-19
 */

@Slf4j
public class MvcLoggingRebinderEndpointFilter extends OncePerRequestFilter implements Ordered {

    private final List<PathPattern> _pathPatterns = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MvcLoggingRebinderEndpointFilter() {
        this("/logging");
    }

    public MvcLoggingRebinderEndpointFilter(String... paths) {
        for (String path : paths) {
            _pathPatterns.add(PathPatternParser.defaultInstance.parse(path));
        }
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (_pathPatterns.stream().anyMatch(pathPattern -> pathPattern.matches(PathContainer.parsePath(servletPath)))) {
            if (HttpMethod.PUT.matches(request.getMethod())) {
                if (MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(request.getContentType()))) {
                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                    ServletInputStream inputStream = request.getInputStream();
                    byte[] bytes = inputStream.readAllBytes();
                    JsonNode jsonNode = readTree(bytes);
                    jsonNode.fields().forEachRemaining(field -> {
                        String key = field.getKey();
                        JsonNode value = field.getValue();
                        LoggingSystemUtils.setLevel(key, value.textValue());
                    });
                    response.getWriter().write("OK");
                    return;
                }
            } else if (HttpMethod.GET.matches(request.getMethod())) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String[] loggers = request.getParameterValues("logger");
                loggers = Objects.requireNonNullElseGet(loggers, () -> new String[0]);
                Map<String, String> loggerLevelMap = new HashMap<>();
                for (String logger : loggers) {
                    LogLevel logLevel = LoggingSystemUtils.getLogLevel(logger);
                    loggerLevelMap.put(logger, logLevel.name());
                }
                response.getWriter().write(JsonUtils.toJson(loggerLevelMap));
                return;
            }
        }
        filterChain.doFilter(request, response);
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
