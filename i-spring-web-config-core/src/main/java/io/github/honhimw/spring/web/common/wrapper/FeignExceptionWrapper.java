package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import feign.FeignException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class FeignExceptionWrapper extends SingleExceptionWrapper<FeignException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull FeignException e) {
        return "{feign.error}";
    }

    @Override
    protected int _httpCode(@NonNull FeignException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
