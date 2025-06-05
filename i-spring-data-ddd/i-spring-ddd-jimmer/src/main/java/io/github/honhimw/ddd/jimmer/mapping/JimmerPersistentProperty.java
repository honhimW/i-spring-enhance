package io.github.honhimw.ddd.jimmer.mapping;

import jakarta.annotation.Nullable;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.springframework.data.mapping.PersistentProperty;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public interface JimmerPersistentProperty extends PersistentProperty<JimmerPersistentProperty> {

    @Nullable
    ImmutableProp getImmutableProp();

}
