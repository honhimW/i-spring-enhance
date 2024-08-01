package io.github.honhimw.spring.web.common;

import io.github.honhimw.spring.Result;
import jakarta.annotation.Nonnull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-01-11
 */

public interface ExceptionWrapper extends Ordered {

    String UNKNOWN_ERROR = "UNKNOWN ERROR";

    /**
     * Determine whether to execute
     *
     * @param e Exception
     * @return wrap if true, pass if false
     */
    boolean support(@Nonnull Throwable e);

    /**
     * Wrap exception as an result
     *
     * @param e the very exception that supported
     * @return the wrapped result should contain exception information
     */
    @Nonnull
    Object wrap(@Nonnull Throwable e);

    /**
     * Only the supported exception by current ExceptionWrapper would be invoke,
     * {@link Throwable#getCause()} result will be use to recheck {{@link #support(Throwable)}} by all ExceptionWrapper again.
     *
     * @return if true, {@link Throwable#getCause()} result will be use
     * to recheck {{@link #support(Throwable)}} by all ExceptionWrapper again.
     * If false, means the very exception will be wrapped by current ExceptionWrapper.
     */
    default boolean unwrapCause() {
        return false;
    }

    /**
     * @see Result#code()
     */
    default String resultCode() {
        return "500";
    }

    /**
     * @see Result#code()
     */
    default String resultCode(Throwable e) {
        return resultCode();
    }

    /**
     * HTTP status
     */
    default int httpCode() {
        return HttpStatus.OK.value();
    }

    /**
     * HTTP status
     */
    default int httpCode(Throwable e) {
        return httpCode();
    }

    /**
     * Order in Collections of Exception.
     * If there are multiple ExceptionWrapper capable of handling the same type of exception are present in the collection.
     * Obviously, the wrapper higher up in the order will be used.
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    ExceptionWrapper DEFAULT = new ExceptionWrapper() {
        @Override
        public boolean support(@Nonnull Throwable e) {
            return true;
        }

        @Nonnull
        @Override
        public Result<Void> wrap(@Nonnull Throwable e) {
            String msg = Optional.ofNullable(MDC.get("traceId"))
                .map(traceId -> UNKNOWN_ERROR + ", TRACE: " + traceId)
                .orElse(UNKNOWN_ERROR);
            return Result.error(resultCode(), msg);
        }

        @Override
        public int httpCode() {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        @Override
        public String resultCode() {
            return String.valueOf(httpCode());
        }
    };

}