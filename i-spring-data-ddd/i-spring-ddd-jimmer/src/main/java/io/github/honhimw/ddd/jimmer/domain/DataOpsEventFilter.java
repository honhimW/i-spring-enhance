package io.github.honhimw.ddd.jimmer.domain;

import org.babyfish.jimmer.Draft;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.ast.table.Props;
import org.babyfish.jimmer.sql.filter.FilterArgs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public class DataOpsEventFilter implements DraftInterceptor<ImmutableSpi, Draft> {

    @Override
    public void beforeSave(@NotNull Draft draft, @Nullable ImmutableSpi original) {
        DraftInterceptor.super.beforeSave(draft, original);
    }



}
