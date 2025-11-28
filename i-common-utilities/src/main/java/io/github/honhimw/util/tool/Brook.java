package io.github.honhimw.util.tool;

import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import io.github.honhimw.util.tool.Lambdas.*;

/**
 * Allow non-runtime exception and no return value operation of {@link Optional}
 *
 * @author hon_him
 * @since 2022-07-28
 */
@SuppressWarnings("unused")
@Slf4j
public final class Brook<O> {

    public static <T> Brook<T> with(T value) {
        return new Brook<>(value, null);
    }

    @SuppressWarnings("all")
    public static <T> Brook<T> fromOptional(Optional<T> optional) {
        Objects.requireNonNull(optional);
        return new Brook<>(optional.orElse(null), null);
    }

    public static <E extends Exception> void runThrow(E e) throws E {
        throw e;
    }

    public static <R, E extends Exception> R callThrow(E e) throws E {
        throw e;
    }

    private final O value;

    private final Exception exception;

    private Brook(O value, Exception exception) {
        this.value = value;
        this.exception = exception;
        if (log.isTraceEnabled()) {
            if (Objects.nonNull(value)) {
                log.trace("Brook with Value: {}", value);
            } else if (Objects.nonNull(exception)) {
                log.trace("Brook with Error: {}", exception.toString());
            }
        }
    }

    private <R> Brook<R> construct(R value, Exception exception) {
        return new Brook<>(value, exception);
    }

    private <R> Brook<R> constructNext(R value) {
        return new Brook<>(value, this.exception);
    }

    private <R> Brook<R> constructError(Exception error) {
        return new Brook<>(null, error);
    }

    private Brook<O> setContext(Object ctx) {
        return new Brook<>(this.value, this.exception);
    }

    public <R, E extends Exception> Brook<R> map(Fn<O, R, E> fn) {
        Objects.requireNonNull(fn);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                return constructNext(fn.apply(value));
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    public <R, E extends Exception> Brook<R> flatMap(Fn<O, Brook<R>, E> fn) {
        Objects.requireNonNull(fn);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                return fn.apply(value);
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    public <R, E extends Exception> Brook<R> mapAndThrow(Fn<O, R, Exception> fn, Fn<Exception, E, E> boxErr) throws E {
        Objects.requireNonNull(fn);
        Objects.requireNonNull(boxErr);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                return constructNext(fn.apply(value));
            } catch (Exception e) {
                throw boxErr.apply(e);
            }
        }
    }

    /**
     * If the condition does not meet, the data will be lost, combined with {@link #fissionFuns(Fn[])}
     */
    public <R, E extends Exception> Brook<R> mapWhen(Predicate<O> p, Fn<O, R, Exception> fn) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(fn);
        if (!isPresent()) {
            return empty();
        } else {
            if (p.test(value)) {
                return map(fn);
            } else {
                return empty();
            }
        }
    }

    public Brook<O> filter(Predicate<O> p) {
        Objects.requireNonNull(p);
        if (!isPresent()) {
            return empty();
        } else {
            if (p.test(value)) {
                return this;
            } else {
                return empty();
            }
        }
    }

    public <R, E extends Exception> Brook<R> flatOptional(Fn<O, Optional<R>, E> fn) {
        Objects.requireNonNull(fn);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                return fromOptional(fn.apply(value));
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    public <R> Brook<R> cast(Class<R> clz) {
        Objects.requireNonNull(clz);
        if (isPresent()) {
            if (clz.isAssignableFrom(value.getClass())) {
                return map(clz::cast);
            }
        }
        return empty();
    }

    public <E extends Exception> Brook<O> exec(Cs<O, E> cs) {
        Objects.requireNonNull(cs);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                cs.accept(value);
                return this;
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    public <E extends Exception> Brook<O> execWhen(Predicate<O> predicate, Cs<O, E> cs) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(cs);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                if (predicate.test(value)) {
                    cs.accept(value);
                }
                return this;
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    @SafeVarargs
    public final <E extends Exception> Brook<O> fissionCons(Cs<Brook<O>, E>... css) {
        Objects.requireNonNull(css);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                for (Cs<Brook<O>, E> cs : css) {
                    cs.accept(this);
                }
                return this;
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    public <R, E extends Exception> Brook<R> fission(Fn<Brook<O>, Brook<R>, E> fn) {
        Objects.requireNonNull(fn);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                Brook<R> apply = fn.apply(this);
                return Objects.isNull(apply) ? empty() : apply;
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    /**
     * Switch like operation, return the value or error once it is obtained
     */
    @SafeVarargs
    public final <R, E extends Exception> Brook<R> fissionFuns(Fn<Brook<O>, Brook<? extends R>, E>... fns) {
        Objects.requireNonNull(fns);
        if (!isPresent()) {
            return empty();
        } else {
            try {
                Brook<? extends R> apply = null;
                for (Fn<Brook<O>, Brook<? extends R>, E> fn : fns) {
                    apply = fn.apply(this);
                    if (apply.isPresent() || apply.isErr()) {
                        break;
                    }
                }
                return Objects.isNull(apply) ? empty() : apply.map(r -> (R) r);
            } catch (Exception e) {
                return constructError(e);
            }
        }
    }

    public <R extends Exception, E extends Exception> Brook<O> breakPoint(Fn<Exception, R, E> fn) throws R, E {
        if (isPresent()) {
            return this;
        } else {
            if (isErr()) {
                throw fn.apply(exception);
            } else {
                throw fn.apply(new NoSuchElementException("No value present"));
            }
        }
    }

    public <R extends Exception, E extends Exception> Brook<O> breakPoint(Predicate<O> predicate, Fn<O, R, E> fn) throws R, E {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(fn);
        if (isPresent()) {
            if (predicate.test(value)) {
                throw fn.apply(value);
            }
        }
        return this;
    }

    public Brook<O> errExec(Cs<Exception, Exception> cs) {
        Objects.requireNonNull(cs);
        if (isErr()) {
            try {
                cs.accept(exception);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    public Brook<O> errReset(Fn<Exception, O, Exception> errMap) {
        Objects.requireNonNull(errMap);
        if (isErr()) {
            try {
                return construct(errMap.apply(exception), null);
            } catch (Exception e) {
                return constructError(e);
            }
        }
        return this;
    }

    public <E extends Exception> Brook<O> errReset(Class<E> errType, Fn<E, O, Exception> errMap) {
        Objects.requireNonNull(errType);
        Objects.requireNonNull(errMap);
        if (isErr()) {
            if (errType.isAssignableFrom(exception.getClass())) {
                try {
                    return construct(errMap.apply(errType.cast(exception)), null);
                } catch (Exception e) {
                    return constructError(e);
                }
            }
        }
        return this;
    }

    /**
     * @return exists value or throw {@link NoSuchElementException}
     * @deprecated use {@link #unwrap()} instead to provide a more accurate naming
     */
    @Deprecated
    public O get() {
        if (Objects.nonNull(value)) {
            return value;
        } else {
            throw new NoSuchElementException("No value present");
        }
    }

    /**
     * If a value is present, returns the value, otherwise throws NoSuchElementException.
     *
     * @return the value currently held by this Brook
     * @throws NoSuchElementException if no value is present
     */
    public O unwrap() {
        if (Objects.nonNull(value)) {
            return value;
        } else {
            throw new NoSuchElementException("No value present");
        }
    }

    public O orElse(O other) {
        if (Objects.nonNull(value)) {
            return value;
        } else {
            return other;
        }
    }

    public O orElse(Sp<O, RuntimeException> sp) {
        if (Objects.nonNull(value)) {
            return value;
        } else {
            return sp.get();
        }
    }

    public <E extends Exception> O errElse(Fn<Exception, O, E> fn) throws E {
        if (isPresent()) {
            return value;
        } else {
            if (isErr()) {
                return fn.apply(exception);
            } else {
                return fn.apply(new NoSuchElementException("No value present"));
            }
        }
    }

    public <R extends Exception, E extends Exception> O orElseThrow(Sp<R, E> sp) throws E, R {
        if (isPresent()) {
            return value;
        } else {
            throw sp.get();
        }
    }

    public <R extends Exception, E extends Exception> O errElseThrow(Fn<Exception, R, E> fn) throws E, R {
        if (isPresent()) {
            return value;
        } else {
            if (isErr()) {
                throw fn.apply(exception);
            } else {
                throw fn.apply(new NoSuchElementException("No value present"));
            }
        }
    }

    public boolean isPresent() {
        return Objects.nonNull(this.value);
    }

    public boolean isEmpty() {
        return !isPresent();
    }

    public boolean isErr() {
        return Objects.nonNull(this.exception);
    }

    public Optional<O> optional() {
        if (isPresent()) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    public Stream<O> stream() {
        if (isPresent()) {
            return Stream.of(value);
        } else {
            return Stream.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <R> Brook<R> empty() {
        if (Objects.isNull(value)) {
            return (Brook<R>) this;
        } else {
            return construct(null, exception);
        }
    }

}
