package io.github.honhimw.spring.cache;

import io.github.honhimw.spring.cache.memory.CacheAlreadyRefreshEvent;
import io.github.honhimw.spring.cache.memory.CacheContext;
import io.github.honhimw.spring.cache.memory.RefreshableCache;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.SmartApplicationListener;

import java.util.Set;

/**
 * @author hon_him
 * @since 2023-06-28
 */

class RefreshableCacheHandler implements SmartApplicationListener, ApplicationEventPublisherAware {

    private final ObjectProvider<RefreshableCache> refreshableCache;

    private final ICacheProperties iCacheProperties;

    private ApplicationEventPublisher publisher;

    public RefreshableCacheHandler(ObjectProvider<RefreshableCache> refreshableCache, ICacheProperties iCacheProperties) {
        this.refreshableCache = refreshableCache;
        this.iCacheProperties = iCacheProperties;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        Set<Class<? extends ApplicationEvent>> refreshOnEvent = iCacheProperties.getRefreshOnEvent();
        if (CollectionUtils.isNotEmpty(refreshOnEvent)) {
            return refreshOnEvent.stream().anyMatch(aClass -> aClass.isAssignableFrom(eventType) && !CacheAlreadyRefreshEvent.class.isAssignableFrom(aClass));
        } else {
            return false;
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
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
        refreshableCache.orderedStream().forEach(refreshableCache -> refreshableCache.cache(context));
    }

}
