package io.github.honhimw.ddd.jpa.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Objects;

/**
 * <pre>
 *
 * </pre>
 *
 * @author hon_him
 * @since 2023-12-28
 */

@SuppressWarnings("all")
public abstract class ExtendJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    protected EntityPathResolver entityPathResolver;
    protected final EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    protected JpaQueryMethodFactory queryMethodFactory;

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public ExtendJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nullable
    protected abstract Object[] getExtendArgs();

    @Nullable
    protected Class<?> overrideRepositoryBaseClass() {
        return null;
    }

    @Autowired
    @Override
    public void setEntityPathResolver(@NonNull ObjectProvider<EntityPathResolver> resolver) {
        super.setEntityPathResolver(resolver);
        this.entityPathResolver = resolver.getIfAvailable(() -> SimpleEntityPathResolver.INSTANCE);
    }

    @Autowired(required = false)
    @Override
    public void setQueryMethodFactory(@Nullable JpaQueryMethodFactory factory) {
        super.setQueryMethodFactory(factory);
        if (factory != null) {
            this.queryMethodFactory = factory;
        }
    }

    @NonNull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory(@NonNull EntityManager entityManager) {
        ExtendConstructorRepositoryFactory jpaRepositoryFactory = new ExtendConstructorRepositoryFactory(entityManager, getExtendArgs(), overrideRepositoryBaseClass());
        jpaRepositoryFactory.setEntityPathResolver(entityPathResolver);
        jpaRepositoryFactory.setEscapeCharacter(escapeCharacter);


        if (queryMethodFactory != null) {
            jpaRepositoryFactory.setQueryMethodFactory(queryMethodFactory);
        }

        return jpaRepositoryFactory;
    }

    private static class ExtendConstructorRepositoryFactory extends JpaRepositoryFactory {

        private final Object[] extendArgs;

        private final Class<?> baseClass;

        /**
         * Creates a new {@link JpaRepositoryFactory}.
         *
         * @param entityManager must not be {@literal null}
         */
        public ExtendConstructorRepositoryFactory(EntityManager entityManager, Object[] extendArgs, Class<?> baseClass) {
            super(entityManager);
            this.extendArgs = extendArgs;
            this.baseClass = baseClass;
        }

        @NonNull
        @Override
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, @NonNull EntityManager entityManager) {
            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
            Object[] args = {entityInformation, entityManager};
            if (ArrayUtils.isNotEmpty(extendArgs)) {
                args = ArrayUtils.insert(args.length, args, extendArgs);
            }
            Object repository = getTargetRepositoryViaReflection(information, args);

            Assert.isInstanceOf(JpaRepositoryImplementation.class, repository);

            return (JpaRepositoryImplementation<?, ?>) repository;
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            if (Objects.nonNull(baseClass)) {
                return baseClass;
            }
            return super.getRepositoryBaseClass(metadata);
        }
    }

}
