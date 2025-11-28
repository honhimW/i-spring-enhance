package io.github.honhimw.ddd.jpa.acl;

import io.github.honhimw.ddd.jpa.util.ExtendJpaRepositoryFactoryBean;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

public class AclJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends ExtendJpaRepositoryFactoryBean<T, S, ID> {

    private AclProvider aclProvider;

    @Autowired
    public void setAclProvider(ObjectProvider<AclProvider> provider) {
        this.aclProvider = provider.getIfAvailable(DefaultAclProviderImpl::new);
    }

    public AclJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nullable
    @Override
    protected Object[] getExtendArgs() {
        return new Object[]{aclProvider};
    }

    @Nullable
    @Override
    protected Class<?> overrideRepositoryBaseClass() {
        return AclJpaRepositoryImpl.class;
    }
}