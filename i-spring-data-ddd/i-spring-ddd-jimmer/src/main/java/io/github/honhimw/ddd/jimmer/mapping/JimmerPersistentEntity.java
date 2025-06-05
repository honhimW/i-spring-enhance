package io.github.honhimw.ddd.jimmer.mapping;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.springframework.data.mapping.PersistentEntity;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public interface JimmerPersistentEntity<T> extends PersistentEntity<T, JimmerPersistentProperty> {

    ImmutableProp getIdProp();

}
