package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.common.ResourceMod;
import org.jspecify.annotations.NonNull;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

/**
 * @author hon_him
 * @since 2025-05-30
 */

public interface AclProvider {

    @NonNull
    AclExecutor getExecutor(JSqlClientImplementor sqlClient, TableProxy<?> tableProxy, String dataDomain, ResourceMod defaultMod);

}
