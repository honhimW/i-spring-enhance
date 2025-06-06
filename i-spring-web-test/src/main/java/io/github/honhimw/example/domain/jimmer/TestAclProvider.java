package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.common.ResourceMod;
import io.github.honhimw.ddd.jimmer.acl.AclExecutor;
import io.github.honhimw.ddd.jimmer.acl.AclProvider;
import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.stereotype.Component;

/**
 * @author honhimW
 * @since 2025-06-03
 */

@Component
public class TestAclProvider implements AclProvider {
    @Override
    @Nonnull
    public AclExecutor getExecutor(
        JSqlClientImplementor sqlClient,
        TableProxy<?> tableProxy,
        String dataDomain,
        ResourceMod defaultMod) {
        return new TestAclExecutor<>(sqlClient, tableProxy, dataDomain, defaultMod);
    }
}
