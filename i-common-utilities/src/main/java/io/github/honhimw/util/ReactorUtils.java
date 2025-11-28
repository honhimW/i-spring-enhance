package io.github.honhimw.util;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2021-08-30
 */
@SuppressWarnings("unused")
@Slf4j
public class ReactorUtils {

    public static <T, R> List<R> block(int parallel, Collection<T> tList, Function<T, Stream<R>> mapper) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        try {
            return block(executor, parallel, tList, null, mapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Run in parallel and return all results.
     *
     * @param executor executor
     * @param parallel max parallel
     * @param tList    source data
     * @param timeout  timeout
     * @param mapper   task
     * @param <T>      source type
     * @param <R>      result type
     * @return result set
     */
    public static <T, R> List<R> block(Executor executor, int parallel, Collection<T> tList,
                                       @Nullable Duration timeout,
                                       Function<T, Stream<R>> mapper) {
        ParallelFlux<T> parallelFlux = Flux
            .fromIterable(tList)
            .parallel(parallel)
            .runOn(Schedulers.fromExecutor(executor));
        Mono<List<R>> mono = parallelFlux
            .flatMap(convertReactorMapper(mapper))
            .collectSortedList((o1, o2) -> 0)
            .onErrorContinue(
                (throwable, o) -> log.error("execution error: {}, source: {}", throwable.toString(), o.toString()));
        if (timeout != null && !timeout.equals(Duration.ZERO)) {
            mono = mono.timeout(timeout, Mono.empty());
        }
        return Objects.requireNonNull(mono.block());
    }

    public static void execute(int parallel, int times, Consumer<Integer> consumer) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        try {
            execute(executor, parallel, times, null, consumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    public static void execute(Executor executor, int parallel, int times, @Nullable Duration timeout, Consumer<Integer> consumer) {
        if (times < 1) {
            return;
        }
        ParallelFlux<Integer> parallelFlux = Flux
            .range(0, times)
            .parallel(parallel)
            .runOn(Schedulers.fromExecutor(executor));

        Mono<Void> mono = parallelFlux
            .doOnNext(consumer)
            .then()
            .onErrorContinue(
                (throwable, o) -> log.error("execution error: {}, index: {}", throwable.toString(), o.toString()));
        if (timeout != null && !timeout.equals(Duration.ZERO)) {
            mono = mono.timeout(timeout, Mono.empty());
        }
        mono.block();
    }

    public static <T> void execute(int parallel, List<T> tList, Consumer<T> consumer) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        try {
            execute(executor, parallel, tList, null, consumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    public static <T> void execute(Executor executor, int parallel, List<T> tList, @Nullable Duration timeout, Consumer<T> consumer) {
        ParallelFlux<T> parallelFlux = Flux
            .fromIterable(tList)
            .parallel(parallel)
            .runOn(Schedulers.fromExecutor(executor));
        Mono<List<T>> mono = parallelFlux
            .doOnNext(consumer)
            .collectSortedList((o1, o2) -> 0)
            .onErrorContinue(
                (throwable, o) -> log.error("execution error: {}, source: {}", throwable.toString(), o.toString()));
        if (timeout != null && !timeout.equals(Duration.ZERO)) {
            mono = mono.timeout(timeout, Mono.empty());
        }
        mono.block();
    }

    /**
     * Stream mapper to Reactor mapper
     */
    public static <T, R> Function<T, Publisher<R>> convertReactorMapper(
        Function<T, Stream<R>> mapper) {
        return t -> {
            Stream<? extends R> rStream = mapper.apply(t);
            return Flux.fromStream(rStream);
        };
    }

    public static <T, R> R retry(T param, Fun<? super T, ? extends R> mapper) {
        return retry(3, param, mapper);
    }

    public static <T, R> R retry(int retryCount, T param, Fun<? super T, ? extends R> mapper) {
        return retry(retryCount, param, mapper, r -> true);
    }

    public static <T, R> R retry(int retryCount, T param, Fun<? super T, R> mapper, Predicate<R> predicate) {
        return retry(retryCount, param, mapper, predicate, Stream.of(Throwable.class));
    }

    /**
     * @param retryCount retry count
     * @param param      input
     * @param mapper     {@link RuntimeException(Throwable)}
     * @param predicate  assertion, retry if false
     * @param throwables causes to retry, {@link Stream#empty()}means not retry
     * @param <T>        input type
     * @param <R>        return type
     * @return result
     */
    public static <T, R> R retry(int retryCount, T param, Fun<T, R> mapper, Predicate<R> predicate,
                                 Stream<Class<? extends Throwable>> throwables) {
        AtomicInteger count = new AtomicInteger(0);
        List<Class<? extends Throwable>> causes = throwables.toList();

        if (retryCount < 0) {
            retryCount = 0;
        }
        try {
            Mono<R> mono = Mono.just(param).flatMap(t -> {
                R r;
                try {
                    r = mapper.apply(t);
                } catch (Exception e) {
                    return Mono.error(e);
                }
                boolean result = predicate.test(r);
                if (!result) {
                    return Mono.error(new IllegalArgumentException("assert failed"));
                } else {
                    return Mono.just(r);
                }
            });
            if (retryCount > 0) {
                mono = mono.retryWhen(Retry.max(retryCount).filter(throwable -> {
                    boolean flag = causes
                        .stream().anyMatch(type -> type.isAssignableFrom(throwable.getClass()));
                    if (flag) {
                        count.incrementAndGet();
                    }
                    return flag;
                }));
            }
            return Objects.requireNonNull(mono.block());
        } catch (Exception e) {
            String msg;
            if (count.get() > 0) {
                msg = String.format("repeat %d times, cause: %s", count.get(), e);
            } else {
                msg = String.format("uncaught exception: %s", e);
            }
            throw new RuntimeException(msg);
        }

    }

    @FunctionalInterface
    public interface Fun<T, R> {

        R apply(T t) throws Exception;

    }

}
