package io.github.honhimw.ddl;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;

/**
 * @author honhimW
 * @since 2025-07-09
 */

public class ForeignKey {

    public final io.github.honhimw.ddl.annotations.ForeignKey foreignKey;

    public final ImmutableProp joinColumn;

    public final ImmutableType table;

    public final ImmutableType referencedTable;

    public ForeignKey(io.github.honhimw.ddl.annotations.ForeignKey foreignKey, ImmutableProp joinColumn, ImmutableType table, ImmutableType referencedTable) {
        this.foreignKey = foreignKey;
        this.joinColumn = joinColumn;
        this.table = table;
        this.referencedTable = referencedTable;
    }
}
