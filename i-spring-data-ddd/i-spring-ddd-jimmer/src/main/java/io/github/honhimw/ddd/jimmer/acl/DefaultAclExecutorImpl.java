package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.common.Ace;
import io.github.honhimw.ddd.common.ResourceMod;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author honhimW
 * @since 2025-05-30
 */

public class DefaultAclExecutorImpl<T> extends AbstractAclExecutor<T> {

    public DefaultAclExecutorImpl(JSqlClientImplementor sqlClient, TableProxy<T> tableProxy, String dataDomain, ResourceMod defaultMod) {
        super(sqlClient, tableProxy, dataDomain, defaultMod);
    }

    @Override
    protected boolean guard() {
        return false;
    }

    @Override
    protected @NotNull Map<String, Object> getAttributes() {
        return new HashMap<>();
    }

    @Override
    protected @NotNull List<? extends Ace> getAcl() {
        return new ArrayList<>();
    }
}
