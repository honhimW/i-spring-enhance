package io.github.honhimw.util.tool;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author honhimW
 * @since 2025-08-08
 */

public abstract sealed class Result<T> permits Result.Err, Result.Ok {

    public static Result<Void> ok() {
        return new Ok<>(null);
    }

    public static <T> Result<T> ok(@NonNull T ok) {
        return new Ok<>(ok);
    }

    public static <T> Result<T> err(@NonNull Throwable err) {
        return new Err<>(err);
    }

    public abstract boolean isOk();

    public abstract boolean isErr();

    public abstract <R> R match(@NonNull Function<T, R> onOk, @NonNull Function<Throwable, R> onErr);

    public abstract void match(@NonNull Consumer<T> onOk, @NonNull Consumer<Throwable> onErr);

    public abstract <R> Result<R> map(@NonNull Function<T, R> onOk);

    public abstract Optional<T> option();

    public abstract T unwrap();

    public abstract T unwrapOrDefault(T defaultValue);

    public abstract T unwrapOrDefault(@NonNull Supplier<T> supplier);

    public static final class Ok<T> extends Result<T> {

        /**
         * Nullable
         */
        private final T ok;

        private Ok(@Nullable T ok) {
            this.ok = ok;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public <R> R match(@NonNull Function<T, R> onOk, @NonNull Function<Throwable, R> onErr) {
            return onOk.apply(ok);
        }

        @Override
        public void match(@NonNull Consumer<T> onOk, @NonNull Consumer<Throwable> onErr) {
            onOk.accept(ok);
        }

        @Override
        public <R> Result<R> map(@NonNull Function<T, R> onOk) {
            return new Ok<>(onOk.apply(ok));
        }

        @Override
        public Optional<T> option() {
            return Optional.ofNullable(ok);
        }

        @Override
        public T unwrap() {
            return ok;
        }

        @Override
        public T unwrapOrDefault(T defaultValue) {
            return ok;
        }

        @Override
        public T unwrapOrDefault(@NonNull Supplier<T> supplier) {
            return ok;
        }

        @Override
        public String toString() {
            return "Ok(" + ok + ")";
        }
    }

    public static final class Err<T> extends Result<T> {

        private final Throwable err;

        public Err(Throwable err) {
            this.err = err;
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public <R> R match(@NonNull Function<T, R> onOk, @NonNull Function<Throwable, R> onErr) {
            return onErr.apply(err);
        }

        @Override
        public void match(@NonNull Consumer<T> onOk, @NonNull Consumer<Throwable> onErr) {
            onErr.accept(err);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R> Result<R> map(@NonNull Function<T, R> onOk) {
            return (Result<R>) this;
        }

        @Override
        public Optional<T> option() {
            return Optional.empty();
        }

        @Override
        public T unwrap() {
            throw new IllegalStateException("called `unwrap` on `Result#Err`", err);
        }

        @Override
        public T unwrapOrDefault(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T unwrapOrDefault(@NonNull Supplier<T> supplier) {
            return supplier.get();
        }

        @Override
        public String toString() {
            return "Err(" + err + ")";
        }
    }

}
