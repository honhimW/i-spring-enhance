package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class NPExceptionWrapper extends SingleExceptionWrapper<NullPointerException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull NullPointerException e) {
        return "{npe}";
    }

    @Override
    protected int _httpCode(@NonNull NullPointerException e) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
