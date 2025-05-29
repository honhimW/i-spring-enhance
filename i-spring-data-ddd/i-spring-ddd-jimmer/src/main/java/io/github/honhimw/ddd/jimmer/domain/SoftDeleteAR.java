package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.ddd.common.LogicDelete;
import org.babyfish.jimmer.sql.*;

/**
 * @author hon_him
 * @since 2025-03-20
 */

@MappedSuperclass
public interface SoftDeleteAR extends AggregateRoot, LogicDelete {

    @LogicalDeleted("true")
    @Column(name = "deleted")
    boolean deleted();

    @Version
    @Column(name = "version")
    int version();

}
