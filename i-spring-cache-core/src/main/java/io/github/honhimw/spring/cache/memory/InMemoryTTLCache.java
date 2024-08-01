package io.github.honhimw.spring.cache.memory;

import io.github.honhimw.spring.cache.TTLCache;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2023-06-27
 */
@Slf4j
@SuppressWarnings("unused")
public final class InMemoryTTLCache<K, V> implements Map<K, V>, TTLCache<K, V> {

    public static final long NEVER_EXPIRE = -1;

    private final ScheduledExecutorService _scheduler;

    private Map<K, Entry<V>> _map;

    /**
     * never expire
     */
    private final Duration defaultTTL;


    public static <K, V> InMemoryTTLCache<K, V> newInstance() {
        return newInstance(Duration.ofMillis(NEVER_EXPIRE));
    }

    public static <K, V> InMemoryTTLCache<K, V> newInstance(Duration defaultTTL) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduler.setRemoveOnCancelPolicy(true);
        return new InMemoryTTLCache<>(defaultTTL, scheduler);
    }

    public static <K, V> InMemoryTTLCache<K, V> newInstance(Duration defaultTTL, ScheduledExecutorService scheduler) {
        Objects.requireNonNull(defaultTTL, "default time-to-live must not be null");
        Objects.requireNonNull(scheduler, "scheduler service must not be null");
        return new InMemoryTTLCache<>(defaultTTL, scheduler);
    }

    private InMemoryTTLCache(Duration defaultTTL, ScheduledExecutorService scheduler) {
        this.defaultTTL = defaultTTL;
        this._scheduler = scheduler;
        this._map = new ConcurrentHashMap<>();
    }

    @Override
    public Long getExpire(K key, TimeUnit unit) {
        Entry<V> vEntry = _map.get(key);
        if (Objects.nonNull(vEntry)) {
            long at = vEntry.expireAt();
            long l = at - System.currentTimeMillis();
            return unit.convert(l, TimeUnit.MILLISECONDS);
        }
        return -2L;
    }

    @Override
    public V put(K key, V value) {
        put(key, value, defaultTTL);
        return value;
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        long millis = ttl.toMillis();
        long current = System.currentTimeMillis();
        long expireAt = current + millis;
        if (millis > 0) {
            _map.compute(key, (k, vEntry) -> {
                cancelPrevTask(k, vEntry);
                AtomicReference<ScheduledFuture<?>> futureReference = new AtomicReference<>();
                Entry<V> nEntry = new Entry<>(expireAt, value, futureReference);
                delayRemove(key, millis, futureReference, nEntry);
                return nEntry;
            });
        } else if (millis < 0) {
            _map.compute(key, (k, vEntry) -> {
                cancelPrevTask(k, vEntry);
                AtomicReference<ScheduledFuture<?>> futureReference = new AtomicReference<>(NoOpScheduledFuture.getInstance());
                return new Entry<V>(expireAt, value, futureReference);
            });
        }
    }

    @Nullable
    public V get(Object key) {
        return getOrDefault(key, null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public V getOrDefault(Object key, @Nullable V defaultValue) {
        Entry<V> vEntry = _map.get((K) key);
        return Optional.ofNullable(vEntry).map(Entry::value).orElse(defaultValue);
    }

    public boolean containsKey(Object key) {
        return _map.containsKey(key);
    }

    public void expire(K key, Duration ttl) {
        long millis = ttl.toMillis();
        long current = System.currentTimeMillis();
        long expireAt = current + millis;
        if (millis > 0) {
            _map.computeIfPresent(key, (k, vEntry) -> {
                cancelPrevTask(k, vEntry);
                AtomicReference<ScheduledFuture<?>> futureReference = new AtomicReference<>();
                Entry<V> nEntry = new Entry<>(expireAt, vEntry.value(), futureReference);
                delayRemove(key, millis, futureReference, nEntry);
                return nEntry;
            });
        } else if (millis < 0) {
            _map.computeIfPresent(key, (k, vEntry) -> {
                cancelPrevTask(k, vEntry);
                AtomicReference<ScheduledFuture<?>> futureReference = new AtomicReference<>(NoOpScheduledFuture.getInstance());
                return new Entry<>(NEVER_EXPIRE, vEntry.value(), futureReference);
            });
        }
    }

    private void cancelPrevTask(K k, @Nullable Entry<V> vEntry) {
        Optional.ofNullable(vEntry)
            .map(Entry::scheduledFuture)
            .map(AtomicReference::get)
            .ifPresent(scheduledFuture -> {
                boolean cancel = scheduledFuture.cancel(false);
                if (cancel && log.isDebugEnabled()) {
                    log.debug("[CANCEL TASK]: [key: {}, expireAt: {}] canceled.", k, vEntry.expireAt());
                }
            });
    }

    private void delayRemove(K key, long millis, AtomicReference<ScheduledFuture<?>> futureReference, Entry<V> nEntry) {
        ScheduledFuture<?> schedule = _scheduler.schedule(() -> {
            boolean remove = _map.remove(key, nEntry);
            if (remove) {
                if (log.isDebugEnabled()) {
                    log.debug("[REMOVE KEY]: {}", key);
                }
            }
        }, millis, TimeUnit.MILLISECONDS);
        futureReference.set(schedule);
    }

    @Override
    public int size() {
        return _map.size();
    }

    @Override
    public boolean isEmpty() {
        return _map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public V remove(Object key) {
        return Optional.ofNullable(_map)
            .map(_map -> _map.remove(key))
            .map(vEntry -> {
                cancelPrevTask((K) key, vEntry);
                return vEntry.value();
            })
            .orElse(null);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((k, v) -> put(k, v, defaultTTL));
    }

    @Override
    public void clear() {
        _map.forEach(this::cancelPrevTask);
        _map.clear();
    }

    @Override
    @Nonnull
    public Set<K> keySet() {
        return _map.keySet();
    }

    @Override
    public Collection<V> values() {
        Collection<Entry<V>> values = _map.values();
        return values.stream().map(Entry::value).toList();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, Entry<V>>> entries = _map.entrySet();
        return entries.stream().map(kEntryEntry -> Map.entry(kEntryEntry.getKey(), kEntryEntry.getValue().value())).collect(Collectors.toSet());
    }

    @Override
    public void discard() {
        this.clear();
        _scheduler.shutdown();
        _map = Collections.emptyMap();
    }

    private record Entry<V>(long expireAt, V value, AtomicReference<ScheduledFuture<?>> scheduledFuture) {
        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    private static final class NoOpScheduledFuture<T> implements ScheduledFuture<T> {

        static NoOpScheduledFuture<?> INSTANT = new NoOpScheduledFuture<>();

        @SuppressWarnings("unchecked")
        static <T> NoOpScheduledFuture<T> getInstance() {
            return (NoOpScheduledFuture<T>) INSTANT;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed o) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Nullable
        @Override
        public T get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Nullable
        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

}
