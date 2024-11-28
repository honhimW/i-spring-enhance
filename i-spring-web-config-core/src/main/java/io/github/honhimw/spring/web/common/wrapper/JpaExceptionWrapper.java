package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@Component
@ConditionalOnClass(Entity.class)
public class JpaExceptionWrapper implements ExceptionWrapper.MessageExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof PersistenceException
               || e instanceof SQLException
               || (StringUtils.startsWith(e.getClass().getPackage().getName(), "jakarta.persistence"))
            ;
    }

    @Nonnull
    @Override
    public String wrap(@Nonnull Throwable e) {
        return "{database.persistence.error}";
    }

    @Override
    public int httpCode(Throwable e) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
