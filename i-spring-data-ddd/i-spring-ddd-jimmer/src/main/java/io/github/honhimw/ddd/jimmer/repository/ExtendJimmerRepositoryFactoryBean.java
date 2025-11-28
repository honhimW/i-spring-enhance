package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.mapping.JimmerEntityInformation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * <pre>
 *
 * </pre>
 *
 * @author hon_him
 * @since 2025-05-30
 */

@SuppressWarnings("all")
public abstract class ExtendJimmerRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JimmerRepositoryFactoryBean<T, S, ID> {

    protected EntityPathResolver entityPathResolver;

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public ExtendJimmerRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nullable
    protected abstract Object[] getExtendArgs();

    @Nullable
    protected Class<?> overrideRepositoryBaseClass() {
        return null;
    }

    @NonNull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory(@NonNull JSqlClientImplementor sqlClient) {
        ExtendConstructorRepositoryFactory jimmerRepositoryFactory = new ExtendConstructorRepositoryFactory(sqlClient, getExtendArgs(), overrideRepositoryBaseClass());
        return jimmerRepositoryFactory;
    }

    private static class ExtendConstructorRepositoryFactory extends JimmerRepositoryFactory {

        private final Object[] extendArgs;

        private final Class<?> baseClass;

        /**
         * Creates a new {@link JpaRepositoryFactory}.
         *
         * @param entityManager must not be {@literal null}
         */
        public ExtendConstructorRepositoryFactory(JSqlClientImplementor sqlClient, Object[] extendArgs, Class<?> baseClass) {
            super(sqlClient);
            this.extendArgs = extendArgs;
            this.baseClass = baseClass;
        }

        @NonNull
        @Override
        protected JimmerRepository<?, ?> getTargetRepository(RepositoryInformation information, JSqlClientImplementor sqlClient) {
            JimmerEntityInformation<?, Object> entityInformation = getEntityInformation(information.getDomainType());
            Object[] args = {entityInformation, sqlClient};
            if (ArrayUtils.isNotEmpty(extendArgs)) {
                args = ArrayUtils.insert(args.length, args, extendArgs);
            }
            Object repository = getTargetRepositoryViaReflection(information, args);

            Assert.isInstanceOf(JimmerRepositoryImplementation.class, repository);

            return (JimmerRepositoryImplementation<?, ?>) repository;
        }

        @NonNull
        @Override
        protected Class<?> getRepositoryBaseClass(@NonNull RepositoryMetadata metadata) {
            if (Objects.nonNull(baseClass)) {
                return baseClass;
            }
            return super.getRepositoryBaseClass(metadata);
        }
    }

}
