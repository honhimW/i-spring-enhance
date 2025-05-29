package io.github.honhimw.ddd.jimmer.repository;


import io.github.honhimw.ddd.jimmer.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2025-02-28
 */

public interface JimmerSpecificationExecutor<E> {

    Optional<E> findOne(Specification.Query spec);

    List<E> findAll(Specification.Query spec);

    Page<E> findAll(Specification.Query spec, Pageable pageable);

    List<E> findAll(Specification.Query spec, Sort sort);

    long count(Specification.Query spec);

    long delete(Specification.Delete spec);

    <S extends E, R> R findBy(Specification.Query spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);

}
