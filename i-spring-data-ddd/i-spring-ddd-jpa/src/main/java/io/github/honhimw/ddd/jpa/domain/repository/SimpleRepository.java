package io.github.honhimw.ddd.jpa.domain.repository;

import io.github.honhimw.ddd.jpa.domain.AbstractAR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author hon_him
 * @since 2022-11-29
 */
@Transactional
@NoRepositoryBean
public interface SimpleRepository<T extends AbstractAR<T, ID>, ID> extends JpaRepository<T, ID>,
    JpaSpecificationExecutor<T> {

    default T update(ID id, Consumer<T> updater) {
        Objects.requireNonNull(updater, "updater should not be null");
        return findById(id)
            .map(t -> {
                updater.accept(t);
                return t;
            })
            .map(this::save)
            .orElseThrow();
    }

}
