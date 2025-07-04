package io.github.honhimw.ddl.dialect;

import io.github.honhimw.ddl.DatabaseVersion;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.dialect.*;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-06-27
 */

public interface DDLDialect extends Dialect {

    static DDLDialect of(Dialect dialect, @Nullable DatabaseVersion version) {
        if (dialect instanceof H2Dialect) {
            return new H2DDLDialect(version);
        } else if (dialect instanceof MySqlDialect) {
            return new MySqlDDLDialect(version);
        } else if (dialect instanceof PostgresDialect) {
            return new PostgresDDLDialect(version);
        } else if (dialect instanceof OracleDialect) {
            return new OracleDDLDialect(version);
        } else if (dialect instanceof SqlServerDialect) {
            return new SqlServerDDLDialect(version);
        } else if (dialect instanceof SQLiteDialect) {
            return new SQLiteDDLDialect(version);
        } else if (dialect instanceof TiDBDialect) {
            return new TiDBDDLDialect(version);
        }
        throw new IllegalArgumentException("Unsupported Dialect: " + dialect);
    }

    default char openQuote() {
        return '"';
    }

    default char closeQuote() {
        return '"';
    }

    /**
     * null -> null
     * "name" -> "name"
     * `name" -> "name"
     * foo bar -> "foo bar"
     * column_name -> column_name
     * @return
     */
    default String quote(String name) {
        if (name == null) {
            return null;
        }
        if (name.charAt(0) == openQuote()) {
            return name;
        }
        if (name.charAt(0) == '`') {
            return openQuote() + name.substring(1, name.length() - 1) + closeQuote();
        }
        if (name.chars().anyMatch(Character::isWhitespace)) {
            return openQuote() + name + closeQuote();
        } else {
            return name;
        }
    }

    default String toQuotedIdentifier(String name) {
        if ( name == null ) {
            return null;
        }

        return openQuote() + name + closeQuote();
    }

    default boolean hasDataTypeInIdentityColumn() {
        return true;
    }

    /**
     * The syntax used during DDL to define a column as being an IDENTITY of
     * a particular type.
     *
     * @param type The {@link java.sql.Types} type code.
     * @return The appropriate DDL fragment.
     */
    default String getIdentityColumnString(int type) {
        return "";
    }

    default String getNullColumnString() {
        return "";
    }

    String columnType(int jdbcType, @Nullable Long length, @Nullable Integer precision, @Nullable Integer scale);

    default long getDefaultLength(int jdbcType) {
        return 255L;
    }

    default int getDefaultScale(int jdbcType) {
        return 0;
    }

    default int getDefaultTimestampPrecision(int jdbcType) {
        return 6;
    }

    default int getDefaultDecimalPrecision(int jdbcType) {
        return 38;
    }

    default int getFloatPrecision(int jdbcType) {
        return 24;
    }

    default int getDoublePrecision(int jdbcType) {
        return 53;
    }

    default String getColumnComment(String comment) {
        return "";
    }

    default String getTableComment(String comment) {
        return "";
    }

    default boolean supportsCommentOn() {
        return true;
    }

    default boolean supportsColumnCheck() {
        return true;
    }

    default boolean supportsTableCheck() {
        return true;
    }

    default String getCheckCondition(String columnName, long min, long max) {
        return quote(columnName) + " between " + min + " and " + max;
    }

    default String getCheckCondition(String columnName, List<String> values) {
        StringBuilder check = new StringBuilder();
        check.append(quote(columnName)).append(" in (");
        String separator = "";
        boolean nullIsValid = false;
        for (String value : values) {
            if (value == null) {
                nullIsValid = true;
                continue;
            }
            check.append(separator).append('\'').append(value).append('\'');
            separator = ",";
        }
        check.append(')');
        if (nullIsValid) {
            check.append(" or ").append(columnName).append(" is null");
        }
        return check.toString();
    }

    default String getTableTypeString() {
        return "";
    }

    default boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    default boolean supportsIfExistsAfterTableName() {
        return false;
    }

    default String getCascadeConstraintsString() {
        return "";
    }

    int resolveJdbcType(Class<?> type, EnumType.Strategy strategy);

    default String getCreateIndexString(boolean unique) {
        return unique ? "create unique index" : "create index";
    }

}
