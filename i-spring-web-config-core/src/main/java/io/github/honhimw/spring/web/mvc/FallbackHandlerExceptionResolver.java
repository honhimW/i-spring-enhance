package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.web.common.AbstractFallbackHandler;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@Slf4j
public class FallbackHandlerExceptionResolver extends AbstractFallbackHandler implements HandlerExceptionResolver {

    private final HttpMessageConverters httpMessageConverters;

    private final ExceptionWrappers exceptionWrappers;

    private boolean printStacktrace;

    public FallbackHandlerExceptionResolver(HttpMessageConverters httpMessageConverters, ExceptionWrappers exceptionWrappers) {
        this.httpMessageConverters = httpMessageConverters;
        this.exceptionWrappers = exceptionWrappers;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ModelAndView resolveException(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, Object handler, @Nonnull Exception ex) {
        log(ex);
        try {
            Object result = exceptionWrappers.handle(ex, (exceptionWrapper, throwable) -> {
                response.setStatus(exceptionWrapper.httpCode(throwable));
                return exceptionWrapper.wrap(throwable);
            });
            List<HttpMessageConverter<?>> converters = httpMessageConverters.getConverters();
            for (HttpMessageConverter converter : converters) {
                if (converter.canWrite(result.getClass(), MediaType.APPLICATION_JSON)) {
                    ServletServerHttpResponse httpOutputMessage = new ServletServerHttpResponse(response);
                    converter.write(result, MediaType.APPLICATION_JSON, httpOutputMessage);
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("Failure in ExceptionWrapper handling.", e);
            return null;
        }
        return new ModelAndView();
    }
}
