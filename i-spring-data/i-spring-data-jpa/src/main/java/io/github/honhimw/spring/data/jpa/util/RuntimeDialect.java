package io.github.honhimw.spring.data.jpa.util;

import org.hibernate.dialect.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hon_him
 * @since 2024-01-16
 */

@SuppressWarnings("unused")
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

    public static boolean isPostgreSQL() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof PostgreSQLDialect;
    }

    public static boolean isMySQL() {
        Dialect dialect = getTruth();
        if (Objects.isNull(dialect)) {
            return false;
        }
        return dialect instanceof MySQLDialect;
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
        return dialect instanceof SQLServerDialect;
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
        while (dialect instanceof DialectDelegateWrapper delegateWrapper) {
            dialect = delegateWrapper.getWrappedDialect();
        }
        return dialect;
    }

}
