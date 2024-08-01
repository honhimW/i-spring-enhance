package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
public class SpringJsonParserExceptionWrapper extends SingleExceptionWrapper<JsonParseException> {

    @Nonnull
    @Override
    protected String _wrap(@Nonnull JsonParseException e) {
        return "JSON转换异常";
    }


    @Override
    protected int unifyCode(@Nonnull JsonParseException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
