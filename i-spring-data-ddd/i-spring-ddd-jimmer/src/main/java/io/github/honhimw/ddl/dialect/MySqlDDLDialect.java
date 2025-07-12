package io.github.honhimw.ddl.dialect;

import io.github.honhimw.ddl.DDLUtils;
import io.github.honhimw.ddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.MySqlDialect;

import static java.sql.Types.*;

/**
 * @author honhimW
 * @since 2025-06-27
 */

public class MySqlDDLDialect extends DefaultDDLDialect {

    public MySqlDDLDialect() {
        this(null);
    }

    public MySqlDDLDialect(final DatabaseVersion version) {
        this(new MySqlDialect(), version);
    }

    public MySqlDDLDialect(final Dialect dialect, final DatabaseVersion version) {
        super(dialect, version);
    }

    @Override
    public char openQuote() {
        return '`';
    }

    @Override
    public char closeQuote() {
        return '`';
    }

    @Override
    public String getColumnComment(String comment) {
        return " comment '" + comment + "'";
    }

    @Override
    public String getTableComment(String comment) {
        return " comment='" + comment + "'";
    }

    @Override
    public boolean supportsCommentOn() {
        return false;
    }

    @Override
    public boolean supportsColumnCheck() {
        return isSameOrAfter(8);
    }

    @Override
    public String columnType(int jdbcType, Long length, Integer precision, Integer scale) {
        length = getLength(jdbcType, length);
        precision = getPrecision(jdbcType, precision);
        scale = getScale(jdbcType, scale);
        return switch (jdbcType) {
            case BOOLEAN ->
                // HHH-6935: Don't use "boolean" i.e. tinyint(1) due to JDBC ResultSetMetaData
                "bit";
            case TIMESTAMP -> DDLUtils.replace("datetime($p)", null, precision, null);
            case TIMESTAMP_WITH_TIMEZONE -> DDLUtils.replace("timestamp($p)", null, precision, null);
            case NUMERIC ->
                // it's just a synonym
                columnType(DECIMAL, length, precision, scale);

            // on MySQL 8, the nchar/nvarchar types use a deprecated character set
            case NCHAR -> DDLUtils.replace("char($l) character set utf8", length, null, null);
            case NVARCHAR -> DDLUtils.replace("varchar($l) character set utf8", length, null, null);

            // the maximum long LOB length is 4_294_967_295, bigger than any Java string
            case BLOB -> "longblob";
            case NCLOB -> "longtext character set utf8";
            case CLOB -> "longtext";
            default -> super.columnType(jdbcType, length, precision, scale);
        };
    }

    @Override
    public int getFloatPrecision(int jdbcType) {
        return 23;
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "not null auto_increment";
    }

    @Override
    public String getTableTypeString() {
        return "engine=InnoDB";
    }

    @Override
    public String getDropForeignKeyString() {
        return "drop foreign key";
    }

    @Override
    public boolean supportsIfExistsBeforeConstraintName() {
        return false;
    }

    @Override
    public boolean supportsIfExistsAfterDropSequence() {
        return false;
    }

}
