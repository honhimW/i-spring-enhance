package io.github.honhimw.spring.data.jpa.acl;

import io.github.honhimw.spring.data.common.ResourceMod;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

/**
 * @author hon_him
 * @since 2023-12-28
 */

public interface AclProvider {

    @Nonnull
    <T> AclExecutor<T> getExecutor(JpaEntityInformation<T, ?> ei, EntityManager em, String dataDomain, ResourceMod defaultMod);

}
