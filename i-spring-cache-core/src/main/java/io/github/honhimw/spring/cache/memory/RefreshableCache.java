package io.github.honhimw.spring.cache.memory;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.apache.commons.lang3.Strings;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;

/**
 * @author hon_him
 * @since 2023-06-28
 */

public interface RefreshableCache extends Ordered, Comparable<RefreshableCache> {

    /**
     * @return id for current cache
     */
    @NonNull
    String version();

    /**
     * @return if current cache has been reloaded.
     */
    default boolean hasChanged(String version) {
        String current = version();
        return !Strings.CS.equals(current, version);
    }

    /**
     * do the cache logic here.
     * @param ctx context for caching job, hold the event and transmit data together.
     */
    void cache(CacheContext ctx);

    /**
     * obviously, get the data that was cached.
     * @return any
     */
    @Nullable
    default Object getCache() {
        return null;
    }

    /**
     * caching pipeline strategy
     * @return is continue when exception was thrown.
     */
    default boolean continueOnError() {
        return true;
    }

    /**
     * caching order in pipeline
     */
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * @see Order
     * @see Ordered
     */
    @Override
    default int compareTo(@NonNull RefreshableCache o) {
        return AnnotationAwareOrderComparator.INSTANCE.compare(this, o);
    }

}
