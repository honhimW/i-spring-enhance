package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.core.WrappedException;
import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2024-06-17
 */

@Component
public class WrappedExceptionWrapper extends SingleExceptionWrapper<WrappedException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull WrappedException e) {
        return e.getMessage();
    }


    @Override
    protected int _httpCode(@Nonnull WrappedException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public boolean unwrapCause() {
        return true;
    }
}
