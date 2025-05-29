package io.github.honhimw.ddd.jimmer.repository;

import org.springframework.data.repository.query.ListQueryByExampleExecutor;

/**
 * @author hon_him
 * @since 2025-02-28
 */

public interface JimmerRepositoryImplementation<E, ID> extends JimmerRepository<E, ID>, JimmerSpecificationExecutor<E>, ListQueryByExampleExecutor<E> {
}
