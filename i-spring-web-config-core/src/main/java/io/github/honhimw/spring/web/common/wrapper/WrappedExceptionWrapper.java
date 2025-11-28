package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.core.WrappedException;
import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2024-06-17
 */

public class WrappedExceptionWrapper extends SingleExceptionWrapper<WrappedException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull WrappedException e) {
        return e.getMessage();
    }


    @Override
    protected int _httpCode(@NonNull WrappedException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public boolean unwrapCause() {
        return true;
    }
}
