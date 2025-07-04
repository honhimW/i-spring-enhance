package io.github.honhimw.ddl.dialect;

import io.github.honhimw.ddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.H2Dialect;

import static java.sql.Types.*;

/**
 * @author honhimW
 * @since 2025-06-27
 */

public class H2DDLDialect extends DefaultDDLDialect {

    public H2DDLDialect() {
        this(null);
    }

    public H2DDLDialect(final DatabaseVersion version) {
        super(new H2Dialect(), version);
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "not null auto_increment";
    }

    @Override
    public String columnType(int jdbcType, Long length, Integer precision, Integer scale) {
        length = getLength(jdbcType, length);
        precision = getPrecision(jdbcType, precision);
        scale = getScale(jdbcType, scale);
        return switch (jdbcType) {
            case NCHAR -> columnType(CHAR, length, precision, scale);
            case NVARCHAR -> columnType(VARCHAR, length, precision, scale);
            default -> super.columnType(jdbcType, length, precision, scale);
        };
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return isSameOrAfter(1, 4);
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return !supportsIfExistsBeforeTableName();
    }

    @Override
    public String getCascadeConstraintsString() {
        return "cascade";
    }
}
