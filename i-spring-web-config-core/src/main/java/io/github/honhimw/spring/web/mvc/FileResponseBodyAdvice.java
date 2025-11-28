package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.annotation.resolver.FileReturn;
import io.github.honhimw.spring.web.util.BodyWithReturnType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        if (body instanceof BodyWithReturnType) {
            return body;
        } else {
            return new BodyWithReturnType(returnType, body);
        }
    }
}
