package io.github.honhimw.util.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author honhimW
 * @since 2025-09-12
 */

public class ErrorStack {

    public static void log(Throwable throwable, int depth) {
        Logger log = LoggerFactory.getLogger("ERROR_STACK");

        ErrorStack errorStack = new ErrorStack(throwable, depth);
        log.error("{}", errorStack);
    }

    public static void log(Throwable throwable) {
        log(throwable, 8);
    }

    private final Throwable throwable;

    private final int depth;

    private String message;

    public ErrorStack(Throwable throwable) {
        this(throwable,8);
    }

    public ErrorStack(Throwable throwable, int depth) {
        this.throwable = throwable;
        this.depth = depth;
    }

    @Override
    public String toString() {
        if (message != null) {
            return message;
        }
        final Map<Integer, Integer> hashCodes = new HashMap<>();
        final StringBuilder sb = new StringBuilder();

        Throwable cause = this.throwable;

        for (int i = 0; i < this.depth; i++) {
            if (cause == null) {
                break;
            }
            int hashCode = System.identityHashCode(cause);
            if (!hashCodes.isEmpty()) {
                sb.append(" -> ");
            }
            if (hashCodes.containsKey(hashCode)) {
                sb.append("...(circular reference to ").append(hashCodes.get(hashCode)).append(')');
                cause = null;
                break;
            }
            sb
                .append('[')
                .append(i)
                .append(": ")
                .append(cause.getClass().getName())
                .append(": ")
                .append(cause.getMessage())
                .append(']');

            hashCodes.put(hashCode, i);
            cause = cause.getCause();
        }
        if (cause != null) {
            sb.append(" -> ...(max depth: ").append(this.depth).append(')');
        }
        message = sb.toString();
        return message;
    }

}
