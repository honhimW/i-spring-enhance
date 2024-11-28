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
public class SpringDataExceptionWrapper implements ExceptionWrapper.MessageExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof DataAccessException
            || (StringUtils.startsWith(e.getClass().getPackage().getName(), "org.springframework.dao"))
            || (StringUtils.startsWith(e.getClass().getPackage().getName(), "org.springframework.transaction"))
            ;
    }

    @Nonnull
    @Override
    public String wrap(@Nonnull Throwable e) {
        return "{database.error}";
    }

    @Override
    public int httpCode(Throwable e) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
