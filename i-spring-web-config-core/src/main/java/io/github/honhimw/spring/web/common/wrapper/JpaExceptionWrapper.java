package io.github.honhimw.spring.web.common.wrapper;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import org.jspecify.annotations.NonNull;
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
    public boolean support(@NonNull Throwable e) {
        return e instanceof PersistenceException
               || e instanceof SQLException
               || (Strings.CS.startsWith(e.getClass().getPackage().getName(), "jakarta.persistence"))
            ;
    }

    @NonNull
    @Override
    public String wrap(@NonNull Throwable e) {
        return "{database.persistence.error}";
    }

    @Override
    public int httpCode(Throwable e) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
