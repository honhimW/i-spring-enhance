package io.github.honhimw.ddd.jimmer.repository;

import jakarta.annotation.Nonnull;
import lombok.Setter;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;

/**
 * @author hon_him
 * @since 2025-01-13
 */

@Setter
public class JimmerRepositoryFactoryBean<R extends Repository<E, ID>, E, ID> extends TransactionalRepositoryFactoryBeanSupport<R, E, ID> {

    private JSqlClientImplementor sqlClient;

    public JimmerRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
        this.setLazyInit(false);
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new JimmerRepositoryFactory(sqlClient);
    }

}
