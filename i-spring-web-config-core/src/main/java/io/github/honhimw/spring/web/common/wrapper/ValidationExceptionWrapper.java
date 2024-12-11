package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ValidationExceptionWrapper extends SingleExceptionWrapper<ValidationException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull ValidationException e) {
        return e.getMessage();
    }

    @Override
    protected int _httpCode(@Nonnull ValidationException e) {
        return HttpStatus.BAD_REQUEST.value();
    }
}
