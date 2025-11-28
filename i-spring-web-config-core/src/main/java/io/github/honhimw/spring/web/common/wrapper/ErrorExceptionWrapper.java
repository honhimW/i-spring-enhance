package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

import java.util.NoSuchElementException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ErrorExceptionWrapper implements ExceptionWrapper.MessageExceptionWrapper {

    @Override
    public boolean support(@NonNull Throwable e) {
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
