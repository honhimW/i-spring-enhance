package io.github.honhimw.ddd.jimmer.entities;

import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import io.github.honhimw.jddl.anno.TableDef;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;

/**
 * @author honhimW
 * @since 2025-06-26
 */

@Entity
@Table(name = CompositeIdDO.TABLE_NAME)
@TableDef(
    comment = "复合id表"
)
public interface CompositeIdDO extends BaseAR {

    String TABLE_NAME = "composite_id";

    @Id
    CompositeId id();

    @Nullable
    String name();

}
