package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import jakarta.validation.ValidationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnClass(jakarta.validation.Validation.class)
public class ValidationExceptionWrapper extends SingleExceptionWrapper<ValidationException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull ValidationException e) {
        return e.getMessage();
    }

    @Override
    protected int unifyCode(@Nonnull ValidationException e) {
        return HttpStatus.BAD_REQUEST.value();
    }
}
