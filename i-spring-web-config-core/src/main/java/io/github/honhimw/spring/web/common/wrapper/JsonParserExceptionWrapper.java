package io.github.honhimw.spring.web.common.wrapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnClass(JsonParser.class)
public class JsonParserExceptionWrapper extends SingleExceptionWrapper<JsonParseException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull JsonParseException e) {
        return "JSON Parse Error";
    }

    @Override
    protected int _httpCode(@Nonnull JsonParseException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
