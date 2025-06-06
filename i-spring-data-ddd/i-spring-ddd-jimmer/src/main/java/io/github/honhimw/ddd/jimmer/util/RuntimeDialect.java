package io.github.honhimw.ddd.jimmer.util;

import org.babyfish.jimmer.sql.dialect.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author honhimW
 * @since 2025-06-06
 */

public class RuntimeDialect {

    private static final RuntimeDialect INSTANCE = new RuntimeDialect();

    private RuntimeDialect() {
    }

    private final AtomicReference<Dialect> dialectHolder = new AtomicReference<>();

    public static void setDialect(Dialect dialect) {
        INSTANCE.dialectHolder.set(dialect);
    }

    public static Dialect getDialect() {
        return INSTANCE.dialectHolder.get();
    }

    public static boolean isPgSQL() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof PostgresDialect;
    }

    public static boolean isMySQL() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof MySqlDialect;
    }

    public static boolean isH2() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof H2Dialect;
    }

    public static boolean isSqlServer() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof SqlServerDialect;
    }

    public static boolean isOracle() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof OracleDialect;
    }

    private static Dialect getTruth() {
        Dialect dialect = INSTANCE.dialectHolder.get();
        while (dialect instanceof DialectDelegate delegate) {
            dialect = delegate.delegate;
        }
        return dialect;
    }

}
