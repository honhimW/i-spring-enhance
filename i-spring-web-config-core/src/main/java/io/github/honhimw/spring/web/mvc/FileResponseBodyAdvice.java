package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.annotation.resolver.FileReturn;
import io.github.honhimw.spring.web.util.BodyWithReturnType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author hon_him
 * @since 2024-08-12
 */

public class FileResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, @Nullable Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(FileReturn.class);
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  @Nonnull MethodParameter returnType,
                                  @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull ServerHttpRequest request,
                                  @Nonnull ServerHttpResponse response) {
        if (body instanceof BodyWithReturnType) {
            return body;
        } else {
            return new BodyWithReturnType(returnType, body);
        }
    }
}
