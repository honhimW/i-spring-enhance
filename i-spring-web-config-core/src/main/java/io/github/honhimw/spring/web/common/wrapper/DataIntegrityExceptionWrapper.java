package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class DataIntegrityExceptionWrapper extends SingleExceptionWrapper<DataIntegrityViolationException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull DataIntegrityViolationException e) {
        if (e instanceof DuplicateKeyException) {
            return "{database.duplicate-key}";
        }
        return "{database.violation-constraint}";
    }

    @Override
    protected int _httpCode(@Nonnull DataIntegrityViolationException e) {
        return HttpStatus.CONFLICT.value();
    }

}
