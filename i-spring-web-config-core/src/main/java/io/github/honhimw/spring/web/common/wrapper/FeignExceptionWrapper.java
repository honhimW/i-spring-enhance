package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import feign.Feign;
import feign.FeignException;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class FeignExceptionWrapper extends SingleExceptionWrapper<FeignException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull FeignException e) {
        return "{feign.error}";
    }

    @Override
    protected int _httpCode(@Nonnull FeignException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
