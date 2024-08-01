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
public class PanicExceptionWrapper extends AbstractExceptionWrapper implements ExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof NullPointerException
            || e instanceof IndexOutOfBoundsException
            || e instanceof Error;
    }

    @Override
    public String message(Throwable e) {
        return "panic!";
    }

    @Override
    public int httpCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
