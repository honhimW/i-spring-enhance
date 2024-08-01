package io.github.honhimw.spring.data.jpa.domain.repository;

import io.github.honhimw.spring.data.jpa.domain.AbstractLogicDeleteAR;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hon_him
 * @since 2022-10-17
 */
@Transactional
@NoRepositoryBean
public interface BaseRepository<T extends AbstractLogicDeleteAR<T, ID>, ID> extends SimpleRepository<T, ID> {

    default void logicDelete(ID id) {
        findById(id).map(AbstractLogicDeleteAR::logicDelete).ifPresent(this::save);
    }

    default void logicDelete(T t) {
        findBy(Example.of(t), FetchableFluentQuery::first).map(AbstractLogicDeleteAR::logicDelete).ifPresent(this::save);
    }

    default void logicDeleteAll(Iterable<ID> ids) {
        findAllById(ids).stream().map(AbstractLogicDeleteAR::logicDelete).forEach(this::save);
    }

}
