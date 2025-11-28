package io.github.honhimw.ddd.jimmer.mapping;

import org.jspecify.annotations.NonNull;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.repository.core.EntityInformation;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public class JimmerEntityInformation<T, ID> implements EntityInformation<T, ID> {

    private final JimmerPersistentEntity<T> persistentEntity;

    public JimmerEntityInformation(JimmerPersistentEntity<T> persistentEntity) {
        this.persistentEntity = persistentEntity;
    }

    @Override
    public boolean isNew(@NonNull T entity) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ID getId(@NonNull T entity) {
        IdentifierAccessor identifierAccessor = persistentEntity.getIdentifierAccessor(entity);
        return (ID) identifierAccessor.getIdentifier();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Class<ID> getIdType() {
        return (Class<ID>) this.persistentEntity.getIdProp().getReturnClass();
    }

    @NonNull
    @Override
    public Class<T> getJavaType() {
        return this.persistentEntity.getType();
    }
}
