package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.ddd.jimmer.repository.JimmerRepository;
import io.github.honhimw.ddd.jimmer.repository.JimmerSpecificationExecutor;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.ListQueryByExampleExecutor;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2025-03-07
 */

@NoRepositoryBean
public interface SimpleRepository<T extends AggregateRoot, ID> extends JimmerRepository<T, ID>, JimmerSpecificationExecutor<T>, ListQueryByExampleExecutor<T> {

    default T update(ID id, Function<T, T> updater) {
        Objects.requireNonNull(updater, "updater should not be null");
        return findById(id)
            .map(updater)
            .map(t -> save(t, SaveMode.UPDATE_ONLY).getModifiedEntity())
            .orElseThrow();
    }

}
