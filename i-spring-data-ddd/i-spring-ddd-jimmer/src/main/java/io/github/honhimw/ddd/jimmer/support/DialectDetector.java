package io.github.honhimw.ddd.jimmer.support;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.dialect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public interface DialectDetector {

    @Nullable
    Dialect detectDialect(@Nonnull Connection con);

    DialectDetector INSTANCE = new Impl();

    class Impl implements DialectDetector {

        private static final Logger LOGGER = LoggerFactory.getLogger(DialectDetector.class);

        @Nullable
        @Override
        public Dialect detectDialect(@Nonnull Connection con) {
            try {
                String productName = JdbcUtils.commonDatabaseName(
                    extractDatabaseMetaData(con, DatabaseMetaData::getDatabaseProductName));
                DatabaseDriver driver = DatabaseDriver.fromProductName(productName);
                return getDialectFromDriver(driver);
            } catch (MetaDataAccessException e) {
                LOGGER.warn("Failed to autodetect jimmer dialect", e);
                return null;
            }
        }

        private static <T> T extractDatabaseMetaData(
            @Nonnull Connection con,
            @Nonnull DatabaseMetaDataCallback<T> action
        ) throws MetaDataAccessException {
            try {
                DatabaseMetaData metaData = con.getMetaData();
                if (metaData == null) {
                    // should only happen in test environments
                    throw new MetaDataAccessException("DatabaseMetaData returned by Connection [" + con + "] was null");
                }
                return action.processMetaData(metaData);
            } catch (CannotGetJdbcConnectionException ex) {
                throw new MetaDataAccessException("Could not get Connection for extracting meta-data", ex);
            } catch (SQLException ex) {
                throw new MetaDataAccessException("Error while extracting DatabaseMetaData", ex);
            } catch (AbstractMethodError err) {
                throw new MetaDataAccessException(
                    "JDBC DatabaseMetaData method not implemented by JDBC driver - upgrade your driver", err);
            }
        }

    }

    @Nullable
    static Dialect getDialectFromDriver(@Nonnull DatabaseDriver driver) {
        return switch (driver) {
            case POSTGRESQL -> new PostgresDialect();
            case ORACLE -> new OracleDialect();
            case MYSQL -> new MySqlDialect();
            case SQLSERVER -> new SqlServerDialect();
            case H2 -> new H2Dialect();
            case SQLITE -> new SQLiteDialect();
            default -> null;
        };
    }

}
