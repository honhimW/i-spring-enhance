package io.github.honhimw.util;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2024-02-23
 */

@Slf4j
public class ThreadUtils {

    public static <T, R> List<R> block(int parallel, Collection<T> tList, Function<T, Stream<R>> mapper) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        try {
            return block(executor, parallel, tList, null, mapper);
        } finally {
            executor.shutdownNow();
        }
    }

    public static <T, R> List<R> block(Executor executor, int parallel,
                                       Collection<@Nullable T> tList,
                                       @Nullable Duration timeout,
                                       Function<@Nullable T, Stream<R>> mapper) {
        CompletionService<Stream<R>> completionService = new ExecutorCompletionService<>(executor);
        int times = tList.size();
        final boolean willTimeout = Objects.nonNull(timeout);
        long timeoutAt = willTimeout ? System.currentTimeMillis() + timeout.toMillis() : 0;
        final BlockingQueue<Optional<T>> queue = new ArrayBlockingQueue<>(times);
        tList.stream().map(Optional::ofNullable).forEach(queue::add);
        final List<Stream<R>> results = new ArrayList<>(times);
        final List<Future<?>> futures = new ArrayList<>(times);
        try {
            Iterator<T> iterator = tList.iterator();
            for (int i = 0; i < parallel; i++) {
                Future<Stream<R>> future = completionService.submit(() -> {
                    Stream<R> toReturn = Stream.empty();
                    while (true) {
                        Optional<T> poll = queue.poll();
                        if (Objects.isNull(poll)) {
                            break;
                        }
                        T t = poll.orElse(null);
                        try {
                            Stream<R> rStream = mapper.apply(t);
                            toReturn = Stream.concat(toReturn, rStream);
                        } catch (Exception e) {
                            log.error("execute error: {}, source: {}", e.getMessage(), t, e);
                        }
                    }
                    return toReturn;
                });
                futures.add(future);
            }
            while (true) {
                Future<Stream<R>> poll = completionService.poll(50, TimeUnit.MILLISECONDS);
                if (Objects.nonNull(poll)) {
                    if (poll.isDone()) {
                        results.add(poll.get());
                    }
                }
                if (queue.isEmpty()) {
                    break;
                }
                if (willTimeout && System.currentTimeMillis() > timeoutAt) {
                    for (Future<?> future : futures) {
                        if (!future.isDone()) {
                            future.cancel(true);
                        }
                    }
                    throw new IllegalStateException("parallel execution timeout, remain: [%d].".formatted(queue.size()));
                }
            }
            return results.stream().flatMap(stream -> stream).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void execute(int parallel, int times, Consumer<Integer> consumer) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        try {
            execute(executor, parallel, times, null, consumer);
        } finally {
            executor.shutdownNow();
        }
    }

    public static void execute(Executor executor, int parallel, int times, @Nullable Duration timeout, Consumer<Integer> consumer) {
        block(executor, parallel, IntStream.range(0, times).boxed().collect(Collectors.toList()), timeout, integer -> {
            assert integer != null;
            consumer.accept(integer);
            return Stream.empty();
        });
    }

    public static <T> void execute(int parallel, List<T> tList, Consumer<T> consumer) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        try {
            execute(executor, parallel, tList, null, consumer);
        } finally {
            executor.shutdownNow();
        }
    }

    public static <T> void execute(Executor executor, int parallel, List<T> tList, @Nullable Duration timeout, Consumer<@Nullable T> consumer) {
        block(executor, parallel, tList, timeout, t -> {
            consumer.accept(t);
            return Stream.empty();
        });
    }

}
