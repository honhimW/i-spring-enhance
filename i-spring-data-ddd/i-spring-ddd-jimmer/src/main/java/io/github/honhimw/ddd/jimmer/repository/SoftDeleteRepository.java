package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.domain.SimpleRepository;
import io.github.honhimw.ddd.jimmer.domain.SoftDeleteAR;
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author hon_him
 * @since 2022-10-17
 */

@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteAR, ID> extends SimpleRepository<T, ID> {

    default void softDelete(ID id) {
        deleteById(id, DeleteMode.LOGICAL);
    }

    default void softDelete(T t) {
        delete(t, DeleteMode.LOGICAL);
    }

    default void softDeleteAll(Iterable<ID> ids) {
        deleteAllById(ids, DeleteMode.LOGICAL);
    }

}
