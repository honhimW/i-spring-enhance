package io.github.honhimw.util.tool;

import java.util.concurrent.Callable;
import java.util.function.*;

/**
 * @author honhimW
 * @since 2025-08-18
 */

public class Lambdas {

    @FunctionalInterface
    public interface Run<E extends Exception> {
        void run() throws E;

        default Runnable jdk() {
            return () -> {
                try {
                    this.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @FunctionalInterface
    public interface Call<R, E extends Exception> {
        R call() throws E;

        default Callable<R> jdk() {
            return this::call;
        }
    }

    @FunctionalInterface
    public interface Fn<T, R, E extends Exception> {
        R apply(T t) throws E;

        default Function<T, R> jdk() {
            return t -> {
                try {
                    return this.apply(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }

    }

    @FunctionalInterface
    public interface Sp<R, E extends Exception> {
        R get() throws E;

        default Supplier<R> jdk() {
            return () -> {
                try {
                    return this.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @FunctionalInterface
    public interface Cs<T, E extends Exception> {
        void accept(T t) throws E;

        default Consumer<T> jdk() {
            return t -> {
                try {
                    this.accept(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @FunctionalInterface
    public interface BiFn<T, U, R, E extends Exception> {
        R apply(T t, U u) throws E;

        default BiFunction<T, U, R> jdk() {
            return (t, u) -> {
                try {
                    return this.apply(t, u);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @FunctionalInterface
    public interface BiCs<T, U, E extends Exception> {
        void accept(T t, U u) throws E;

        default BiConsumer<T, U> jdk() {
            return (t, u) -> {
                try {
                    this.accept(t, u);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

}
