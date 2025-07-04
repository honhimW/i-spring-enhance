package io.github.honhimw.ddl.dialect;

import io.github.honhimw.ddl.DDLUtils;
import io.github.honhimw.ddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.H2Dialect;

import static java.sql.Types.*;

/**
 * @author honhimW
 * @since 2025-06-27
 */

public class SqlServerDDLDialect extends DefaultDDLDialect {

    public SqlServerDDLDialect() {
        this(null);
    }

    public SqlServerDDLDialect(final DatabaseVersion version) {
        super(new H2Dialect(), version);
    }

    @Override
    public char openQuote() {
        return '[';
    }

    @Override
    public char closeQuote() {
        return ']';
    }

    @Override
    public String columnType(int jdbcType, Long length, Integer precision, Integer scale) {
        length = getLength(jdbcType, length);
        precision = getPrecision(jdbcType, precision);
        scale = getScale(jdbcType, scale);
        return switch (jdbcType) {
            case BOOLEAN -> "bit";

            case TINYINT ->
                //'tinyint' is an unsigned type in Sybase and
                //SQL Server, holding values in the range 0-255
                //see HHH-6779
                "smallint";
            case INTEGER ->
                //it's called 'int' not 'integer'
                "int";
            case DOUBLE -> "float";

            case DATE -> "date";
            case TIME -> "time";
            case TIMESTAMP -> DDLUtils.replace("datetime2($p)", null, precision, null);
            case TIME_WITH_TIMEZONE, TIMESTAMP_WITH_TIMEZONE -> DDLUtils.replace("datetimeoffset($p)", null, precision, null);

            case BLOB -> "varbinary(max)";
            case CLOB -> "varchar(max)";
            case NCLOB -> "nvarchar(max)";

            case SQLXML -> "xml";

            default -> super.columnType(jdbcType, length, precision, scale);
        };
    }

    @Override
    public boolean supportsCommentOn() {
        return false;
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "identity not null";
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return isSameOrAfter(16);
    }

    @Override
    public String getCreateIndexString(boolean unique) {
        // we only create unique indexes, as opposed to unique constraints,
        // when the column is nullable, so safe to infer unique => nullable
        return unique ? "create unique nonclustered index" : "create index";
    }
}
