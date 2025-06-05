package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.mapping.JimmerEntityInformation;
import io.github.honhimw.ddd.jimmer.mapping.JimmerPersistentEntityImpl;
import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class JimmerRepositoryFactory extends RepositoryFactorySupport {

    private final JSqlClientImplementor sqlClient;

    public JimmerRepositoryFactory(JSqlClientImplementor sqlClient) {
        this.sqlClient = sqlClient;
    }

    @Nonnull
    @Override
    public <T, ID> JimmerEntityInformation<T, ID> getEntityInformation(@Nonnull Class<T> domainClass) {
        return new JimmerEntityInformation<>(new JimmerPersistentEntityImpl<>(TypeInformation.of(domainClass)));
    }

    @Nonnull
    @Override
    protected JimmerRepository<?, ?> getTargetRepository(@Nonnull RepositoryInformation metadata) {
        return getTargetRepository(metadata, sqlClient);
    }

    protected JimmerRepository<?, ?> getTargetRepository(RepositoryInformation information, JSqlClientImplementor sqlClient) {
        Class<?> repositoryInterface = information.getRepositoryInterface();
        JimmerEntityInformation<?, Object> entityInformation = getEntityInformation(information.getDomainType());
        Assert.state(JimmerRepository.class.isAssignableFrom(repositoryInterface), "Target repository is not a jimmer repository");
        if (repositoryInterface.getTypeParameters().length != 0) {
            throw new IllegalStateException(
                "Illegal repository interface \"" +
                repositoryInterface.getName() +
                "\", It itself must not contain any generic parameters, " +
                "because it must solidify the generic parameters for the super interface \"" +
                JimmerRepository.class.getName() +
                "\""
            );
        }

        return getTargetRepositoryViaReflection(information, entityInformation, sqlClient);
    }

    @Nonnull
    @Override
    protected Class<?> getRepositoryBaseClass(@Nonnull RepositoryMetadata metadata) {
        return SimpleJimmerRepository.class;
    }

    @Nonnull
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
        QueryLookupStrategy.Key key,
        @Nonnull ValueExpressionDelegate valueExpressionDelegate) {
        return Optional.empty();
    }
}
