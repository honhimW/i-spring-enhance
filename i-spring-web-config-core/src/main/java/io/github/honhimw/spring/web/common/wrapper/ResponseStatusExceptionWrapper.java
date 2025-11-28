package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class ResponseStatusExceptionWrapper extends SingleExceptionWrapper<ResponseStatusException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull ResponseStatusException e) {
        return StringUtils.getIfBlank(e.getReason(), () -> {
            HttpStatus resolve = HttpStatus.resolve(e.getStatusCode().value());
            return Objects.nonNull(resolve) ? resolve.getReasonPhrase() : UNKNOWN_ERROR;
        });
    }

    @Override
    protected int _httpCode(@NonNull ResponseStatusException e) {
        return e.getStatusCode().value();
    }
}
