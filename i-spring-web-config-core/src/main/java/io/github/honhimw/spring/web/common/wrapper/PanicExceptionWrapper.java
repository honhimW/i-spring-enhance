package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
public class PanicExceptionWrapper implements ExceptionWrapper.MessageExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof NullPointerException
            || e instanceof IndexOutOfBoundsException
            || e instanceof Error;
    }

    @Nonnull
    @Override
    public String wrap(@Nonnull Throwable e) {
        return "PANIC!";
    }

    @Override
    public int httpCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
