package io.github.honhimw.spring.cache.memory;

import org.springframework.context.ApplicationEvent;

/**
 * Published after cache refreshed.
 * @author hon_him
 * @since 2023-06-28
 */

public final class CacheAlreadyRefreshEvent extends ApplicationEvent {

    public CacheAlreadyRefreshEvent(Object source) {
        super(source);
    }

}
