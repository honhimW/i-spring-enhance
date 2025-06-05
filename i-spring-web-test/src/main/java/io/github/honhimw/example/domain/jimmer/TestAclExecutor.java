package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.core.MatchingType;
import io.github.honhimw.ddd.common.Ace;
import io.github.honhimw.ddd.common.ResourceMod;
import io.github.honhimw.ddd.jimmer.acl.AbstractAclExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public class TestAclExecutor<T> extends AbstractAclExecutor<T> {

    public TestAclExecutor(JSqlClientImplementor sqlClient, TableProxy<T> tableProxy, String dataDomain, ResourceMod defaultMod) {
        super(sqlClient, tableProxy, dataDomain, defaultMod);
    }

    @Override
    protected boolean guard() {
        return false;
    }

    @Override
    protected boolean isRoot() {
        return false;
    }

    @Override
    protected @NotNull Map<String, Object> getAttributes() {
        return Map.of(
            "ln", "Harker"
        );
    }

    @Override
    protected @NotNull List<? extends Ace> getAcl() {
        Ace ace = new IAce("player", ResourceMod.RWX, "fullName.lastName", "{{ln}}", MatchingType.EQUAL, null);
        return List.of(
            ace
        );
    }

    @AllArgsConstructor
    @Getter
    private static class IAce implements Ace {
        private final String dataDomain;
        private final ResourceMod mod;
        private final String name;
        private final String value;
        private final MatchingType matchingType;
        private final String targetValue;
    }
}
