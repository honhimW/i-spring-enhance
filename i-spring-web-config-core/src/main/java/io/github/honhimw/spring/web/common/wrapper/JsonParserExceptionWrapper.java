package io.github.honhimw.spring.web.common.wrapper;

import com.fasterxml.jackson.core.JsonParseException;
import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class JsonParserExceptionWrapper extends SingleExceptionWrapper<JsonParseException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull JsonParseException e) {
        return "{json.parse.error}";
    }

    @Override
    protected int _httpCode(@Nonnull JsonParseException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
