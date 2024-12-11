package io.github.honhimw.spring.cache.redis;

import io.github.honhimw.spring.cache.TTLCache;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author hon_him
 * @since 2023-06-28
 */

public class RedisTTLCache<K, V> implements TTLCache<K, V> {

    private final RedisTemplate<K, V> redisTemplate;

    public RedisTTLCache(RedisTemplate<K, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void expire(K key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    @Nullable
    @Override
    public Long getExpire(K key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    @Override
    public void discard() {
        redisTemplate.discard();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey((K) key));
    }

    @Nullable
    @Override
    public V get(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public V put(K key, V value) {
        redisTemplate.opsForValue().set(key, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public V remove(Object key) {
        boolean has = false;
        V v = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(v)) {
            has = true;
            redisTemplate.delete((K) key);
        }
        return has ? v : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        redisTemplate.opsForValue().multiSet(m);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
