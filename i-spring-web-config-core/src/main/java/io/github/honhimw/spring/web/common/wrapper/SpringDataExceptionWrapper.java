package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnClass(DataAccessException.class)
public class SpringDataExceptionWrapper extends AbstractExceptionWrapper implements ExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof DataAccessException
            || (StringUtils.startsWith(e.getClass().getPackage().getName(), "org.springframework.dao"))
            || (StringUtils.startsWith(e.getClass().getPackage().getName(), "org.springframework.transaction"))
            ;
    }

    @Override
    public String message(Throwable e) {
        return "database error";
    }

    @Override
    public int httpCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
