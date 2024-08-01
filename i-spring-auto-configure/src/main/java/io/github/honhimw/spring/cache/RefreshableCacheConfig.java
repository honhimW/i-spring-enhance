package io.github.honhimw.spring.cache;

import io.github.honhimw.spring.cache.memory.CacheAlreadyRefreshEvent;
import io.github.honhimw.spring.cache.memory.CacheContext;
import io.github.honhimw.spring.cache.memory.RefreshableCache;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.SmartApplicationListener;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * @author hon_him
 * @since 2023-06-28
 */

class RefreshableCacheConfig implements SmartApplicationListener, ApplicationEventPublisherAware {

    private final List<RefreshableCache> refreshableCaches;

    private final ICacheProperties iCacheProperties;

    private ApplicationEventPublisher publisher;

    public RefreshableCacheConfig(List<RefreshableCache> refreshableCaches, ICacheProperties iCacheProperties) {
        this.refreshableCaches = refreshableCaches;
        this.iCacheProperties = iCacheProperties;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @Override
    public boolean supportsEventType(@Nonnull Class<? extends ApplicationEvent> eventType) {
        Set<Class<? extends ApplicationEvent>> refreshOnEvent = iCacheProperties.getRefreshOnEvent();
        if (CollectionUtils.isNotEmpty(refreshOnEvent)) {
            return refreshOnEvent.stream().anyMatch(aClass -> aClass.isAssignableFrom(eventType) && !CacheAlreadyRefreshEvent.class.isAssignableFrom(aClass));
        } else {
            return false;
        }
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationEvent event) {
        synchronized (this) {
            CacheContext context = new CacheContext(event);
            try {
                invokeCache(context);
                publisher.publishEvent(new CacheAlreadyRefreshEvent(event));
            } finally {
                context.close();
            }
        }
    }

    protected void invokeCache(CacheContext context) {
        if (CollectionUtils.isNotEmpty(refreshableCaches)) {
            List<RefreshableCache> sorted = refreshableCaches.stream().sorted().toList();
            sorted.forEach(refreshableCache -> refreshableCache.cache(context));
        }
    }

}
