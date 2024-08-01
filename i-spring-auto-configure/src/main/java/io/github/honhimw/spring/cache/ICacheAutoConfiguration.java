package io.github.honhimw.spring.cache;

import io.github.honhimw.spring.cache.memory.RefreshableCache;
import io.github.honhimw.spring.cache.redis.IRedisConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-06-28
 */

@EnableConfigurationProperties(ICacheProperties.class)
@ConditionalOnClass({RedisTemplate.class, TTLCache.class})
@Import({IRedisConfiguration.class, IRedisConfiguration.ReactiveRedisConfig.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
public class ICacheAutoConfiguration {

    @Bean
    @ConditionalOnClass(RefreshableCache.class)
    @ConditionalOnMissingBean
//    @ConditionalOnProperty(name = "i.spring.cache.refresh-on-event")
    RefreshableCacheConfig refreshableCacheConfig(List<RefreshableCache> refreshableCacheList, ICacheProperties iCacheProperties) {
        return new RefreshableCacheConfig(refreshableCacheList, iCacheProperties);
    }

}
