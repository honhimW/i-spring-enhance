package io.github.honhimw.ddd.jimmer.domain;

import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author hon_him
 * @since 2025-03-07
 */

@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteAR, ID> extends SimpleRepository<T, ID> {

    default void softDelete(@Nonnull ID id) {
        deleteById(id, DeleteMode.LOGICAL);
    }

    default void softDelete(@Nonnull T entity) {
        delete(entity, DeleteMode.LOGICAL);
    }

}
