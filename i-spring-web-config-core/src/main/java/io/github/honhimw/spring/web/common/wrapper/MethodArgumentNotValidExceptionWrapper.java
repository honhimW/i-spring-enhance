package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class MethodArgumentNotValidExceptionWrapper extends SingleExceptionWrapper<MethodArgumentNotValidException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull MethodArgumentNotValidException e) {
        return e.getMessage();
    }

    @Override
    protected int _httpCode(@NonNull MethodArgumentNotValidException e) {
        return e.getStatusCode().value();
    }
}
