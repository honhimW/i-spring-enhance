package io.github.honhimw.spring.data.jpa.acl;

import io.github.honhimw.spring.data.common.Ace;
import io.github.honhimw.spring.data.common.ResourceMod;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2023-12-28
 */

public class DefaultAclExecutorImpl<T> extends AbstractAclExecutor<T>{

    public DefaultAclExecutorImpl(ResourceMod defaultMod, JpaEntityInformation<T, ?> ei, EntityManager em, String dataDomain) {
        super(defaultMod, ei, em, dataDomain);
    }

    @Override
    protected boolean guard() {
        return false;
    }

    @Override
    protected boolean isRoot() {
        return false;
    }

    @Nonnull
    @Override
    protected Map<String, Object> getAttributes() {
        return new HashMap<>();
    }

    @Nonnull
    @Override
    protected List<Ace> getAcl() {
        return new ArrayList<>();
    }
}
