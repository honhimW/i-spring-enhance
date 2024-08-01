package io.github.honhimw.spring.data.common;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author hon_him
 * @since 2023-11-14
 */
@SuppressWarnings("unused")
public class Reactors<T> {

    private final T delegate;
    private final Supplier<Scheduler> schedulerSupplier;

    private Reactors(T delegate) {
        this(delegate, Schedulers::boundedElastic);
    }

    public Reactors(T delegate, Supplier<Scheduler> schedulerSupplier) {
        this.delegate = delegate;
        this.schedulerSupplier = schedulerSupplier;
    }

    protected T getDelegate() {
        return delegate;
    }

    public Mono<Void> none(Consumer<T> consumer) {
        return Mono.just(getDelegate())
            .doOnNext(consumer)
            .then()
            .subscribeOn(schedulerSupplier.get());
    }

    public <R> Mono<R> single(Function<T, R> function) {
        return Mono.just(getDelegate())
            .map(function)
            .subscribeOn(schedulerSupplier.get());
    }

    public <R> Mono<R> option(Function<T, Optional<R>> function) {
        return Mono.just(getDelegate())
            .map(function)
            .mapNotNull(r -> r.orElse(null))
            .subscribeOn(schedulerSupplier.get());
    }

    public <R> Flux<R> multiple(Function<T, Iterable<R>> function) {
        return Mono.just(getDelegate())
            .flatMapIterable(function)
            .subscribeOn(schedulerSupplier.get());
    }

    public static <T> Reactors<T> delegate(T delegate) {
        return new Reactors<>(delegate);
    }

    public static Mono<Void> callNone(Runnable runnable) {
        return Mono.fromRunnable(runnable)
            .then()
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static <R> Mono<R> callSingle(Callable<R> callable) {
        return Mono.fromCallable(callable)
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static <R> Mono<R> callOption(Callable<Optional<R>> callable) {
        return Mono.fromCallable(callable)
            .mapNotNull(r -> r.orElse(null))
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static <R> Flux<R> callMultiple(Callable<Iterable<R>> callable) {
        return Mono.fromCallable(callable)
            .flatMapIterable(rs -> rs)
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static <T> Reactors<T> delegate(T delegate, Supplier<Scheduler> schedulerSupplier) {
        return new Reactors<>(delegate, schedulerSupplier);
    }

}
