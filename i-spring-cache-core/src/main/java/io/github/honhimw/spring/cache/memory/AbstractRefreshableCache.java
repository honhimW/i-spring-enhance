package io.github.honhimw.spring.cache.memory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;

import jakarta.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hon_him
 * @since 2023-06-28
 */

@Slf4j
public abstract class AbstractRefreshableCache implements RefreshableCache {

    private volatile String _version = UUID.randomUUID().toString();

    protected abstract void _cache(CacheContext ctx);

    protected boolean supportEvent(ApplicationEvent event) {
        return true;
    }

    protected boolean skipOnEvent(ApplicationEvent event) {
        return false;
    }

    @Nonnull
    @Override
    public final String version() {
        return _version;
    }

    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicReference<String> lastInvoke = new AtomicReference<>();

    @Override
    public final void cache(CacheContext ctx) {
        ApplicationEvent event = ctx.get(CacheContext.EVENT);
        if (log.isDebugEnabled()) {
            log.debug("[{}] current version: {}, trigger event: {}", this.getClass().getSimpleName(), version(), event.getClass().getSimpleName());
        }
        // mark method invoke
        String invokeId = UUID.randomUUID().toString();
        lastInvoke.set(invokeId);
        // if event is accepted
        if (supportEvent(event) && !skipOnEvent(event)) {
            if (lock.tryLock()) {
                try {
                    // ensure that the cache is updated after the last method call
                    while (true) {
                        _cache(ctx);
                        if (!StringUtils.equals(invokeId, lastInvoke.get())) {
                            invokeId = lastInvoke.get();
                            if (log.isDebugEnabled()) {
                                log.debug("re-cache again, cause cache-method has been invoked during last refreshing.");
                            }
                        } else {
                            break;
                        }
                    }
                    _version = UUID.randomUUID().toString();
                } catch (Exception e) {
                    if (!continueOnError()) {
                        throw e;
                    } else {
                        log.debug("[{}] error when cache refresh", this.getClass().getSimpleName(), e);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("cache updating");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("[{}] skip on event: {}", this.getClass().getSimpleName(), event.getClass().getSimpleName());
            }
        }
    }

}
