package io.github.honhimw.example.config;

import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.cache.memory.CacheContext;
import io.github.honhimw.spring.cache.memory.RefreshableCache;
import io.github.honhimw.spring.event.ApplicationBeanReadyEvent;
import io.github.honhimw.spring.web.mvc.MvcHttpLogCondition;
import io.github.honhimw.spring.web.reactive.ReactiveHttpLogCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    BuildIn testBuildIn() {
        log.error("in creation");
        return () -> {
            String foo = redisTemplate.opsForValue().get("foo");
            log.info("redis foo: {}", foo);
        };
    }

    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    RefreshableCache testRefreshableCache(Environment environment) {
        return new RefreshableCache() {
            @NonNull
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

    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    ApplicationListener<ApplicationBeanReadyEvent> applicationListener() {
        return event -> log.info("application started");
    }

    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    TestApi testApi1() {
        log.error("create testApi1");
        return new TestApi() {
        };
    }

    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    TestApi testApi2(@Autowired @Qualifier("testApi1") TestApi testApi1) {
        log.error(testApi1.foo());
        log.error("create testApi2");
        return new TestApi() {
            @Override
            public String foo() {
                return "Bar2";
            }
        };
    }

//    @Bean
    ReactiveHttpLogCondition disableTestAnyLoggingWebFlux() {
        return req -> !Strings.CS.equals(req.getPath().toString(), "/test/any");
    }

//    @Bean
    MvcHttpLogCondition disableTestAnyLoggingMvc() {
        return req -> !Strings.CS.equals(req.getRequestURI(), "/test/any");
    }

    public interface TestApi {
        default String foo() {
            return "bar";
        }
    }

}
