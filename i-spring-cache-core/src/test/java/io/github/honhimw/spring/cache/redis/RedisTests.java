package io.github.honhimw.spring.cache.redis;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.SetOperations;

import java.time.Duration;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@SpringBootTest(classes = RedisApp.class)
public class RedisTests {

    @Test
    @SneakyThrows
    void even() {
        SetOperations<String, String> set = RedisUtils.set(String.class);
        String key = "hello:key";
        set.add(key, "world", "world1", "world2");
        RedisUtils.expire(key, Duration.ofSeconds(1));
        Thread.sleep(2_000L);
    }

}
