package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import jakarta.annotation.Nonnull;
import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class JpaExceptionWrapper implements ExceptionWrapper.MessageExceptionWrapper {

    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof PersistenceException
               || e instanceof SQLException
               || (Strings.CS.startsWith(e.getClass().getPackage().getName(), "jakarta.persistence"))
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
