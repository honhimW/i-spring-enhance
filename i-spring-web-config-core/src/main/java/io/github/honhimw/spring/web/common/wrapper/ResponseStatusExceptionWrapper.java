package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ResponseStatusExceptionWrapper extends SingleExceptionWrapper<ResponseStatusException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull ResponseStatusException e) {
        return StringUtils.getIfBlank(e.getReason(), () -> {
            HttpStatus resolve = HttpStatus.resolve(e.getStatusCode().value());
            return Objects.nonNull(resolve) ? resolve.getReasonPhrase() : UNKNOWN_ERROR;
        });
    }

    @Override
    protected int _httpCode(@Nonnull ResponseStatusException e) {
        return e.getStatusCode().value();
    }
}
