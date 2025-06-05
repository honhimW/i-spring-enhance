package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.jimmer.repository.ExtendJimmerRepositoryFactoryBean;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

public class AclJimmerRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends ExtendJimmerRepositoryFactoryBean<T, S, ID> {

    private AclProvider aclProvider;

    @Autowired
    public void setAclProvider(ObjectProvider<AclProvider> provider) {
        this.aclProvider = provider.getIfAvailable(DefaultAclProviderImpl::new);
    }

    public AclJimmerRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
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
        return AclJimmerRepositoryImpl.class;
    }
}