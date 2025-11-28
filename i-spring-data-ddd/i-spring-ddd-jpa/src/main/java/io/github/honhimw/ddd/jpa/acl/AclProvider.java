package io.github.honhimw.ddd.jpa.acl;

import io.github.honhimw.ddd.common.ResourceMod;
import org.jspecify.annotations.NonNull;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

/**
 * @author hon_him
 * @since 2023-12-28
 */

public interface AclProvider {

    @NonNull
    <T> AclExecutor<T> getExecutor(JpaEntityInformation<T, ?> ei, EntityManager em, String dataDomain, ResourceMod defaultMod);

}
