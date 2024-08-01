package io.github.honhimw.spring;

/**
 * 包装异常
 *
 * @author hon_him
 * @since 2024-06-17
 */

public class WrappedException extends RuntimeException {

    public WrappedException() {
    }

    public WrappedException(String message) {
        super(message);
    }

    public WrappedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrappedException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public WrappedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
