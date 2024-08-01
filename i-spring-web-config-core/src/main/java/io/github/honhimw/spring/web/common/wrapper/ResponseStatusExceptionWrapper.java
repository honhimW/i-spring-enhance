package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnClass(ResponseStatusException.class)
public class ResponseStatusExceptionWrapper extends SingleExceptionWrapper<ResponseStatusException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull ResponseStatusException e) {
        return StringUtils.defaultIfBlank(e.getMessage(), String.valueOf(e.getStatusCode().value()));
    }

    @Override
    protected int unifyCode(@Nonnull ResponseStatusException e) {
        return e.getStatusCode().value();
    }
}
