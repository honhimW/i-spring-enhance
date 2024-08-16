package io.github.honhimw.spring.web.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.core.MethodParameter;

/**
 * @author hon_him
 * @since 2024-08-12
 */

public class BodyWithReturnType {

    @Nonnull
    private final MethodParameter returnType;

    @Nullable
    private final Object body;

    public BodyWithReturnType(@Nonnull MethodParameter returnType, @Nullable Object body) {
        this.returnType = returnType;
        this.body = body;
    }

    @Nonnull
    public MethodParameter getReturnType() {
        return returnType;
    }

    @Nullable
    public Object getBody() {
        return body;
    }
}
