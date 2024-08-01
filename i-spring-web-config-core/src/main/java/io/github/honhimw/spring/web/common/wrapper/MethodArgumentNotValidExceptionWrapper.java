package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnWebApplication
@ConditionalOnClass(MethodArgumentNotValidException.class)
public class MethodArgumentNotValidExceptionWrapper extends SingleExceptionWrapper<MethodArgumentNotValidException> implements ExceptionWrapper {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull MethodArgumentNotValidException e) {
        return e.getMessage();
    }

    @Override
    protected int unifyCode(@Nonnull MethodArgumentNotValidException e) {
        return e.getStatusCode().value();
    }
}
