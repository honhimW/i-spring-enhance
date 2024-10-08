package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
public class NPExceptionWrapper extends SingleExceptionWrapper<NullPointerException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull NullPointerException e) {
        return "NPE";
    }

    @Override
    protected int unifyCode(@Nonnull NullPointerException e) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
