package io.github.honhimw.ddl.dialect;

import io.github.honhimw.ddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.SQLiteDialect;

import static java.sql.Types.*;

/**
 * @author honhimW
 * @since 2025-06-27
 */

public class SQLiteDDLDialect extends DefaultDDLDialect {

    public SQLiteDDLDialect() {
        this(null);
    }

    public SQLiteDDLDialect(final DatabaseVersion version) {
        super(new SQLiteDialect(), version);
    }

    @Override
    public String columnType(int jdbcType, Long length, Integer precision, Integer scale) {
        length = getLength(jdbcType, length);
        precision = getPrecision(jdbcType, precision);
        scale = getScale(jdbcType, scale);
        return switch (jdbcType) {
            case DECIMAL ->
                isSameOrAfter(3) ? super.columnType(jdbcType, length, precision, scale) : columnType(NUMERIC, length, precision, scale);
            case CHAR -> isSameOrAfter(3) ? super.columnType(jdbcType, length, precision, scale) : "char";
            case NCHAR -> isSameOrAfter(3) ? super.columnType(jdbcType, length, precision, scale) : "nchar";
            // No precision support
            case FLOAT -> "float";
            case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE -> "timestamp";
            case TIME_WITH_TIMEZONE -> "time";
            case BINARY, VARBINARY -> "blob";
            default -> super.columnType(jdbcType, length, precision, scale);
        };
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "integer";
    }
}
