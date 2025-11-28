package io.github.honhimw.ddd.jimmer.event;

import io.github.honhimw.ddd.jimmer.domain.AggregateRoot;
import io.github.honhimw.ddd.jimmer.domain.AggregateRootDraft;
import io.github.honhimw.ddd.jimmer.domain.SoftDeleteAR;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;

/**
 * @author honhimW
 * @since 2025-06-04
 */

public class CallbackInterceptor implements DraftInterceptor<AggregateRoot, AggregateRootDraft> {

    private final Callback callback;

    public CallbackInterceptor(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void beforeSave(@NonNull AggregateRootDraft draft, @Nullable AggregateRoot original) {
        if (original == null) {
            // INSERT
            callback.preCreate(draft);
        } else {
            // UPDATE
            if (original instanceof SoftDeleteAR softDelete) {
                if (ImmutableObjects.isLoaded(original, "deleted") && softDelete.isDeleted()) {
                    callback.preSoftRemove(draft);
                    return;
                }
            }
            callback.preUpdate(draft);
        }
    }

}
