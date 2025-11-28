package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class PanicExceptionWrapper implements ExceptionWrapper.MessageExceptionWrapper {

    @Override
    public boolean support(@NonNull Throwable e) {
        return e instanceof NullPointerException
            || e instanceof IndexOutOfBoundsException
            || e instanceof Error;
    }

    @NonNull
    @Override
    public String wrap(@NonNull Throwable e) {
        return "{panic}";
    }

    @Override
    public int httpCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
