package io.github.honhimw.spring.web.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;

/**
 * @author hon_him
 * @since 2024-08-12
 */

public class BodyWithReturnType {

    @NonNull
    private final MethodParameter returnType;

    @Nullable
    private final Object body;

    public BodyWithReturnType(@NonNull MethodParameter returnType, @Nullable Object body) {
        this.returnType = returnType;
        this.body = body;
    }

    @NonNull
    public MethodParameter getReturnType() {
        return returnType;
    }

    @Nullable
    public Object getBody() {
        return body;
    }
}
