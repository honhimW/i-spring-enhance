package org.example.config;

import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.cache.memory.CacheContext;
import io.github.honhimw.spring.cache.memory.RefreshableCache;
import io.github.honhimw.spring.event.ApplicationBeanReadyEvent;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author hon_him
 * @since 2024-11-19
 */

@Slf4j
@Configuration
public class TestBeans {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    BuildIn testBuildIn() {
        log.error("in creation");
        return () -> {
            String foo = redisTemplate.opsForValue().get("foo");
            log.info("redis foo: {}", foo);
        };
    }

    @Bean
    RefreshableCache testRefreshableCache(Environment environment) {
        return new RefreshableCache() {
            @Nonnull
            @Override
            public String version() {
                return "";
            }

            @Override
            public void cache(CacheContext ctx) {
                log.error("hello: {}", environment.getProperty("hello"));
            }
        };
    }

    @Bean()
    ApplicationListener<ApplicationBeanReadyEvent> applicationListener() {
        return event -> log.info("application started");
    }

    public interface TestApi {
        default String foo() {
            return "bar";
        }
    }

}
