package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.common.ResourceMod;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jetbrains.annotations.NotNull;

/**
 * @author honhimW
 * @since 2025-05-30
 */

public class DefaultAclProviderImpl implements AclProvider {

    @Override
    public @NotNull AclExecutor getExecutor(JSqlClientImplementor sqlClient, TableProxy<?> tableProxy, String dataDomain, ResourceMod defaultMod) {
        return new DefaultAclExecutorImpl<>(sqlClient, tableProxy, dataDomain, defaultMod);
    }
}
