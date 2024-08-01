package io.github.honhimw.spring.cache.redis.reactive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import reactor.core.publisher.Mono;

import jakarta.annotation.Nullable;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@SuppressWarnings({"unused"})
public class RxRedisUtils implements ApplicationContextAware {

    private static RxRedisJacksonTemplateFactory rxRedisJacksonTemplateFactory;

    private static ReactiveRedisTemplate<String, String> readRedisTemplate;
    private static ReactiveRedisTemplate<String, Object> writeRedisTemplate;

    private static Duration DEFAULT_TTL;
    private static ObjectMapper MAPPER;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        rxRedisJacksonTemplateFactory = applicationContext.getBean(RxRedisJacksonTemplateFactory.class);
        readRedisTemplate = rxRedisJacksonTemplateFactory.string();
        writeRedisTemplate = rxRedisJacksonTemplateFactory.forType(Object.class);
        MAPPER = rxRedisJacksonTemplateFactory.getMapper();
        DEFAULT_TTL = applicationContext.getEnvironment().getProperty("spring.cache.redis.time-to-live", Duration.class, Duration.ofDays(1));
    }

    public static RedisSerializer<String> keySerializer() {
        return rxRedisJacksonTemplateFactory.keySerializer();
    }

    public static <V> ReactiveRedisTemplate<String, V> getTemplate(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type);
    }

    public static ReactiveRedisTemplate<String, String> readRedisTemplate() {
        return readRedisTemplate;
    }

    public static ReactiveRedisTemplate<String, Object> writeRedisTemplate() {
        return writeRedisTemplate;
    }

    public static Mono<Boolean> putPersist(String key, Object value) {
        return writeRedisTemplate.opsForValue().set(key, value);
    }

    public static Mono<Boolean> putPersistAsString(String key, Object value) {
        try {
            String json = MAPPER.writeValueAsString(value);
            return writeRedisTemplate.opsForValue().set(key, json);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public static Mono<Boolean> put(String key, Object value) {
        return put(key, value, DEFAULT_TTL);
    }

    public static Mono<Boolean> put(String key, Object value, Duration ttl) {
        return writeRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public static Mono<Boolean> putAsString(String key, Object value) {
        return putAsString(key, value, DEFAULT_TTL);
    }

    public static Mono<Boolean> putAsString(String key, Object value, Duration ttl) {
        try {
            String json = MAPPER.writeValueAsString(value);
            return writeRedisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public static Mono<Boolean> containsKey(String key) {
        return writeRedisTemplate.hasKey(key);
    }

    public static Mono<Long> remove(String... key) {
        return writeRedisTemplate.delete(key);
    }

    public static Mono<String> get(String key) {
        return readRedisTemplate.opsForValue().get(key);
    }

    public static <V> Mono<V> get(String key, Type type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, type));
    }

    public static <V> Mono<V> get(String key, Class<V> type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, type));
    }

    public static <V> Mono<V> get(String key, TypeReference<V> type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, type));
    }

    public static <V> Mono<V> get(String key, JavaType type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, type));
    }

    /**
     * @see #putAsString(String, Object, Duration)
     */
    public static <V> Mono<V> getStringAs(String key, Type type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, String.class))
            .mapNotNull(json -> deserialize(json, type));
    }

    public static <V> Mono<V> getStringAs(String key, Class<V> type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, String.class))
            .mapNotNull(json -> deserialize(json, type));
    }

    public static <V> Mono<V> getStringAs(String key, TypeReference<V> type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, String.class))
            .mapNotNull(json -> deserialize(json, type));
    }

    public static <V> Mono<V> getStringAs(String key, JavaType type) {
        return readRedisTemplate.opsForValue().get(key)
            .mapNotNull(json -> deserialize(json, String.class))
            .mapNotNull(json -> deserialize(json, type));
    }

    public static Mono<Boolean> expire(String key, Duration ttl) {
        return writeRedisTemplate.expire(key, ttl);
    }

    public static Mono<Duration> getExpire(String key) {
        return writeRedisTemplate.getExpire(key);
    }

    public static Mono<Boolean> persist(String key) {
        return writeRedisTemplate.persist(key);
    }

    /**
     * Redis 字符串类型
     */
    public static <V> ReactiveValueOperations<String, V> string(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type).opsForValue();
    }

    /**
     * Redis Hash类型
     */
    public static <V> ReactiveHashOperations<String, String, V> map(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type).opsForHash();
    }

    /**
     * Redis 列表类型
     */
    public static <V> ReactiveListOperations<String, V> list(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type).opsForList();
    }

    /**
     * Redis 集合类型
     */
    public static <V> ReactiveSetOperations<String, V> set(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type).opsForSet();
    }

    /**
     * Redis 有序集合类型
     */
    public static <V> ReactiveZSetOperations<String, V> sortedSet(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type).opsForZSet();
    }

    /**
     * Redis Stream API
     */
    public static <V> ReactiveStreamOperations<String, String, V> stream(Class<V> type) {
        return rxRedisJacksonTemplateFactory.forType(type).opsForStream();
    }

    @Nullable
    public static <V> V deserialize(@Nullable String json, Class<V> type) throws SerializationException {
        return deserialize(json, MAPPER.constructType(type));
    }

    @Nullable
    public static <V> V deserialize(@Nullable String json, Type type) throws SerializationException {
        return deserialize(json, MAPPER.constructType(type));
    }

    @Nullable
    public static <V> V deserialize(@Nullable String json, TypeReference<V> typeRef) throws SerializationException {
        return deserialize(json, MAPPER.constructType(typeRef));
    }

    @Nullable
    public static <V> V deserialize(@Nullable String json, JavaType type) throws SerializationException {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception ex) {
            throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

}
