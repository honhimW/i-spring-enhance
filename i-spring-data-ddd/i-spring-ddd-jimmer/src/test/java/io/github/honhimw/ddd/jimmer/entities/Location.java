package io.github.honhimw.ddd.jimmer.entities;

import io.github.honhimw.ddd.jimmer.domain.BaseAR;
import io.github.honhimw.jddl.anno.*;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.sql.*;

/**
 * @author honhimW
 * @since 2025-06-26
 */

@Entity
@Table(name = Location.TABLE_NAME)
@TableDef(
    indexes = {
        @Index(name = "idx_name", columns = {"name"}),
    },
    comment = "地理位置"
)
public interface Location extends BaseAR {

    String TABLE_NAME = "location";

    @Id
    String id();

    @Nullable
    String name();

    @Nullable
    Double lon();

    @Nullable
    Double lat();

}
