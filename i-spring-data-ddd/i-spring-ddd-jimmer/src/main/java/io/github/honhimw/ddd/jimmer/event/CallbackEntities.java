package io.github.honhimw.ddd.jimmer.event;

import io.github.honhimw.ddd.jimmer.support.EntitiesDelegate;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.Entities;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public class CallbackEntities extends EntitiesDelegate {

    public CallbackEntities(Entities delegate) {
        super(delegate);
    }

    @Override
    public <T> @Nullable T findById(Class<T> type, Object id) {
        return super.findById(type, id);
    }


}
