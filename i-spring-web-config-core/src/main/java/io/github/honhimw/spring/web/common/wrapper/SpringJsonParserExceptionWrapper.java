package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class SpringJsonParserExceptionWrapper extends SingleExceptionWrapper<JsonParseException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull JsonParseException e) {
        return "{json.parse.error}";
    }


    @Override
    protected int _httpCode(@NonNull JsonParseException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
