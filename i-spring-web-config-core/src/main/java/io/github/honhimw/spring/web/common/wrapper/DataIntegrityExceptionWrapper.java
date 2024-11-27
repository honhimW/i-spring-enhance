package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnClass(DataIntegrityViolationException.class)
public class DataIntegrityExceptionWrapper extends SingleExceptionWrapper<DataIntegrityViolationException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull DataIntegrityViolationException e) {
        if (e instanceof DuplicateKeyException) {
            return "Duplicate Key";
        }
        return "Violation Constraint";
    }

    @Override
    protected int _httpCode(@Nonnull DataIntegrityViolationException e) {
        return HttpStatus.CONFLICT.value();
    }

}
