package io.github.honhimw.ddd.common;

import java.util.concurrent.Callable;

/**
 * Support temporary authorization to ROOT to deal with the problem of permissions in some scenarios.
 *
 * @author hon_him
 * @since 2024-06-13
 */

public class SudoSupports {

    private static final ThreadLocal<Boolean> SUDO = new InheritableThreadLocal<>();

    public static boolean isSudo() {
        Boolean b = SUDO.get();
        return Boolean.TRUE.equals(b);
    }

    public static void sudo(Runnable runnable) {
        try {
            SUDO.set(true);
            runnable.run();
        } finally {
            SUDO.set(false);
            SUDO.remove();
        }
    }

    public static <R> R sudo(Callable<R> callable) {
        try {
            SUDO.set(true);
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            SUDO.set(false);
            SUDO.remove();
        }
    }

}
