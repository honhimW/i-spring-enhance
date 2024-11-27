package io.github.honhimw.spring.cache;

import io.github.honhimw.spring.cache.memory.RefreshableCache;
import io.github.honhimw.spring.cache.redis.IRedisConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author hon_him
 * @since 2023-06-28
 */

@EnableConfigurationProperties(ICacheProperties.class)
@ConditionalOnClass({RedisTemplate.class, TTLCache.class})
@Import({IRedisConfiguration.class, IRedisConfiguration.ReactiveRedisConfig.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
public class ICacheAutoConfiguration {

    @Bean(value = "refreshableCacheHandler")
    @ConditionalOnClass(RefreshableCache.class)
    @ConditionalOnMissingBean(name = "refreshableCacheHandler")
    @ConditionalOnProperty(name = "i.spring.cache.enabled", havingValue = "true", matchIfMissing = true)
    RefreshableCacheHandler refreshableCacheHandler(ObjectProvider<RefreshableCache> refreshableCacheProvider, ICacheProperties iCacheProperties) {
        return new RefreshableCacheHandler(refreshableCacheProvider, iCacheProperties);
    }

}
