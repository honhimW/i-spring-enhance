package io.github.honhimw.spring.web.util;

import jakarta.annotation.Nullable;
import org.springframework.security.core.context.SecurityContext;

import java.util.Optional;

/**
 * Nullable {@link org.springframework.security.core.context.SecurityContextHolder}
 * @author hon_him
 * @since 2024-07-23
 */

public class ISecurityContextHolder {

    private static final ThreadLocal<SecurityContext> _THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void setContext(SecurityContext context) {
        _THREAD_LOCAL.set(context);
    }

    public static Optional<SecurityContext> getContext() {
        return Optional.ofNullable(_THREAD_LOCAL.get());
    }

    @Nullable
    public static SecurityContext unwrapContext() {
        return _THREAD_LOCAL.get();
    }

    public static void clearContext() {
        _THREAD_LOCAL.remove();
    }

}
