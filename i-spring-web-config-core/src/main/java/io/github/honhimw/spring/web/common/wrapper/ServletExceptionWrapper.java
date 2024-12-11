package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ServletExceptionWrapper extends SingleExceptionWrapper<ServletException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull ServletException e) {
        return e.getMessage();
    }

    @Override
    protected int _httpCode(@Nonnull ServletException e) {
        return HttpStatus.BAD_REQUEST.value();
    }
}
