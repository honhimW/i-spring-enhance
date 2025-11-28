package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class RedisUtils implements ApplicationContextAware {

    private static RedisJacksonTemplateFactory redisJacksonTemplateFactory;

    private static RedisTemplate<String, byte[]> bytesRedisTemplate;

    private static RedisTemplate<String, String> readRedisTemplate;
    private static RedisTemplate<String, Object> writeRedisTemplate;

    private static Duration DEFAULT_TTL;
    private static ObjectMapper MAPPER;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        redisJacksonTemplateFactory = applicationContext.getBean(RedisJacksonTemplateFactory.class);
        bytesRedisTemplate = redisJacksonTemplateFactory.bytes();
        readRedisTemplate = redisJacksonTemplateFactory.string();
        writeRedisTemplate = redisJacksonTemplateFactory.forType(Object.class);
        MAPPER = redisJacksonTemplateFactory.getMapper();
        DEFAULT_TTL = applicationContext.getEnvironment().getProperty("spring.cache.redis.time-to-live", Duration.class, Duration.ofDays(1));
    }

    public static RedisSerializer<String> keySerializer() {
        return redisJacksonTemplateFactory.keySerializer();
    }

    public static <V> RedisTemplate<String, V> getTemplate(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type);
    }

    public static RedisTemplate<String, String> readRedisTemplate() {
        return readRedisTemplate;
    }

    public static RedisTemplate<String, Object> writeRedisTemplate() {
        return writeRedisTemplate;
    }

    public static boolean putPersist(String key, Object value) {
        try {
            writeRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static boolean putPersistAsString(String key, Object value) {
        try {
            String json = MAPPER.writeValueAsString(value);
            writeRedisTemplate.opsForValue().set(key, json);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static boolean put(String key, Object value) {
        return put(key, value, DEFAULT_TTL);
    }

    public static boolean put(String key, Object value, Duration ttl) {
        try {
            writeRedisTemplate.opsForValue().set(key, value, ttl);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static boolean putAsString(String key, Object value) {
        return putAsString(key, value, DEFAULT_TTL);
    }

    public static boolean putAsString(String key, Object value, Duration ttl) {
        try {
            String json = MAPPER.writeValueAsString(value);
            writeRedisTemplate.opsForValue().set(key, json, ttl);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static boolean putBytes(String key, byte[] value) {
        return putBytes(key, value, DEFAULT_TTL);
    }

    public static boolean putBytes(String key, byte[] value, Duration ttl) {
        try {
            bytesRedisTemplate.opsForValue().set(key, value, ttl);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static boolean containsKey(String key) {
        try {
            Boolean b = writeRedisTemplate.hasKey(key);
            return Boolean.TRUE.equals(b);
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static long remove(String... key) {
        try {
            if (key.length > 0) {
                if (key.length == 1) {
                    return Boolean.TRUE.equals(writeRedisTemplate.delete(key[0])) ? 1L : 0L;
                } else {
                    return writeRedisTemplate.delete(Arrays.asList(key));
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return 0L;
    }

    @Nullable
    public static String get(String key) {
        return readRedisTemplate.opsForValue().get(key);
    }

    @Nullable
    public static <V> V get(String key, Type type) {
        String json = readRedisTemplate.opsForValue().get(key);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V get(String key, Class<V> type) {
        String json = readRedisTemplate.opsForValue().get(key);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V get(String key, TypeReference<V> type) {
        String json = readRedisTemplate.opsForValue().get(key);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V get(String key, JavaType type) {
        String json = readRedisTemplate.opsForValue().get(key);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V getStringAs(String key, Type type) {
        String quoteJson = readRedisTemplate.opsForValue().get(key);
        String json = deserialize(quoteJson, String.class);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V getStringAs(String key, Class<V> type) {
        String quoteJson = readRedisTemplate.opsForValue().get(key);
        String json = deserialize(quoteJson, String.class);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V getStringAs(String key, TypeReference<V> type) {
        String quoteJson = readRedisTemplate.opsForValue().get(key);
        String json = deserialize(quoteJson, String.class);
        return deserialize(json, type);
    }

    @Nullable
    public static <V> V getStringAs(String key, JavaType type) {
        String quoteJson = readRedisTemplate.opsForValue().get(key);
        String json = deserialize(quoteJson, String.class);
        return deserialize(json, type);
    }

    @Nullable
    public static byte[] getBytes(String key) {
        return bytesRedisTemplate.opsForValue().get(key);
    }

    @Nullable
    public static Boolean expire(String key, Duration ttl) {
        return writeRedisTemplate.expire(key, ttl);
    }

    @SuppressWarnings("all")
    @Nullable
    public static Duration getExpire(String key) {
        Long expire = writeRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        if (Objects.nonNull(expire)) {
            if (expire == -1) {
                return Duration.ZERO;
            } else if (expire == -2) {
                return null;
            }
            return Duration.ofMillis(expire);
        }
        return null;
    }

    public static Boolean persist(String key) {
        return writeRedisTemplate.persist(key);
    }

    /**
     * Scan Keys with pattern, A better command than `KEYS`.
     * Using count=100 as default scan size
     *
     * @param pattern key pattern, e.g. spring:redis:i-*, *
     * @return keys iterator
     */
    public static Iterator<String> scan(String pattern) {
        return readRedisTemplate.scan(ScanOptions.scanOptions().match(pattern).count(100).build());
    }

    /**
     * Get on batch keys from pattern and count
     *
     * @param pattern key pattern, e.g. spring:redis:i-*, *
     * @param count   max result size
     * @return scan once result
     */
    public static List<String> scan(String pattern, int count) {
        try (Cursor<String> scan = readRedisTemplate.scan(ScanOptions.scanOptions()
            .match(pattern)
            .count(count)
            .build())) {
            List<String> keys = new ArrayList<>(count);
            while (scan.hasNext()) {
                String key = scan.next();
                keys.add(key);
                if (keys.size() == count) {
                    break;
                }
            }
            return keys;
        }
    }

    /**
     * Alias for `SCAN`
     *
     * @param pattern key pattern, e.g. spring:redis:i-*, *
     * @param count   max result size
     * @return scan once result
     * @see #scan(String, int) alias, preffer using `SCAN`
     */
    public static List<String> keys(String pattern, int count) {
        return scan(pattern, count);
    }

    /**
     * Redis String
     */
    public static <V> ValueOperations<String, V> string(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type).opsForValue();
    }

    /**
     * Redis Hash
     */
    public static <V> HashOperations<String, String, V> map(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type).opsForHash();
    }

    /**
     * Redis List
     */
    public static <V> ListOperations<String, V> list(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type).opsForList();
    }

    /**
     * Redis Set
     */
    public static <V> SetOperations<String, V> set(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type).opsForSet();
    }

    /**
     * Redis ZSet
     */
    public static <V> ZSetOperations<String, V> sortedSet(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type).opsForZSet();
    }

    /**
     * Redis Stream API
     */
    public static <V> StreamOperations<String, String, V> stream(Class<V> type) {
        return redisJacksonTemplateFactory.forType(type).opsForStream();
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
