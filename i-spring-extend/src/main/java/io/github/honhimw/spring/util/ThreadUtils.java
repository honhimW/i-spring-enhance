package io.github.honhimw.spring.util;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2024-02-23
 */

public class ThreadUtils {

    public static <T, R> List<R> block(int parallel, Collection<T> tList, Function<T, Stream<R>> mapper) {
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        return block(executor, parallel, tList, null, mapper);
    }

    public static <T, R> List<R> block(Executor executor, int parallel, Collection<T> tList,
                                       Duration timeout,
                                       Function<T, Stream<R>> mapper) {
        int times = tList.size();
        CountDownLatch countDownLatch = new CountDownLatch(times);
        Semaphore semaphore = new Semaphore(parallel);
        final boolean willTimeout = Objects.nonNull(timeout);
        long now = System.currentTimeMillis();
        long timeoutAt = willTimeout ? timeout.toMillis() : -1;
        try {
            Iterator<T> iterator = tList.iterator();
            List<RunnableFuture<Stream<R>>> futures = new ArrayList<>(times);
            for (int i = 0; i < times; i++) {
                final int finalI = i;
                T next = iterator.next();
                if (willTimeout) {
                    if (!semaphore.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                        throw new IllegalStateException("parallel execution timeout, remain: [%d].".formatted(countDownLatch.getCount()));
                    } else {
                        semaphore.acquire();
                    }
                }
                RunnableFuture<Stream<R>> runnableFuture = new FutureTask<>(() -> {
                    try {
                        return mapper.apply(next);
                    } finally {
                        countDownLatch.countDown();
                        semaphore.release();
                    }
                });
                executor.execute(runnableFuture);
                futures.add(runnableFuture);
            }
            if (willTimeout) {
                if (!countDownLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("parallel execution timeout, remain: [%d].".formatted(countDownLatch.getCount()));
                }
            } else {
                countDownLatch.await();
            }
            return futures.stream().flatMap(streamRunnableFuture -> {
                try {
                    if (streamRunnableFuture.isDone()) {
                        return streamRunnableFuture.get();
                    } else {
                        streamRunnableFuture.cancel(true);
                        throw new IllegalStateException("future task are not finished.");
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).toList();
        } catch (InterruptedException e) {
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

    public static void execute(Executor executor, int parallel, int times, Duration timeout, Consumer<Integer> consumer) {
        CountDownLatch countDownLatch = new CountDownLatch(times);
        Semaphore semaphore = new Semaphore(parallel);
        final boolean willTimeout = Objects.nonNull(timeout);
        try {
            for (int i = 0; i < times; i++) {
                final int finalI = i;
                if (willTimeout) {
                    if (!semaphore.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                        throw new IllegalStateException("parallel execution timeout, remain: [%d].".formatted(countDownLatch.getCount()));
                    } else {
                        semaphore.acquire();
                    }
                }
                executor.execute(() -> {
                    try {
                        consumer.accept(finalI);
                    } finally {
                        countDownLatch.countDown();
                        semaphore.release();
                    }
                });
            }
            if (willTimeout) {
                if (!countDownLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("parallel execution timeout, remain: [%d].".formatted(countDownLatch.getCount()));
                }
            } else {
                countDownLatch.await();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
