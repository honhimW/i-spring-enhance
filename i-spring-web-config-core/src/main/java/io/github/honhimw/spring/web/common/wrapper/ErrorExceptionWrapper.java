package io.github.honhimw.spring.web.common.wrapper;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
public class ErrorExceptionWrapper extends AbstractExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof NoSuchElementException
            || e instanceof IllegalArgumentException
            || e instanceof IllegalStateException
            || e instanceof UnsupportedOperationException;
    }

    @Override
    public int httpCode(Throwable e) {
        return HttpStatus.BAD_REQUEST.value();
    }
}
