package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.web.common.ExceptionWrapper;
import jakarta.annotation.Nonnull;

/**
 * @author hon_him
 * @since 2023-09-21
 */

public abstract class AbstractExceptionWrapper implements ExceptionWrapper {

    @Nonnull
    @Override
    public Result<Void> wrap(@Nonnull Throwable e) {
        Result<Void> result = Result.empty();
        result.code(resultCode(e));
        result.msg(message(e));
        return result;
    }

    public String message(Throwable e) {
        return e.getMessage();
    }

    @Override
    public String resultCode(Throwable e) {
        return String.valueOf(httpCode(e));
    }

}
