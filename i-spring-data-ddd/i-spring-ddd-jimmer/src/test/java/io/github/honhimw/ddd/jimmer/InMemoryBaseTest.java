package io.github.honhimw.ddd.jimmer;

import io.github.honhimw.ddd.jimmer.support.SpringConnectionManager;
import io.github.honhimw.ddl.dialect.DDLDialect;
import com.zaxxer.hikari.HikariDataSource;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.JLazyInitializationSqlClient;
import org.babyfish.jimmer.sql.dialect.*;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Function;

/**
 * @author honhimW
 * @since 2025-06-26
 */

public abstract class InMemoryBaseTest {

    private JSqlClientImplementor client;

    protected Dialect dialect;

    @BeforeEach
    void init() {
        if (client == null) {
            DataSource dataSource = newDataSource();
            DB db = using();
            dialect = switch (db) {
                case H2 -> new H2Dialect();
                case MYSQL -> new MySqlDialect();
                case ORACLE -> new OracleDialect();
                case SQL_SERVER -> new SqlServerDialect();
                case TI_DB -> new TiDBDialect();
                case POSTGRESQL -> new PostgresDialect();
                case SQLITE -> new SQLiteDialect();
            };
            JSqlClient.Builder builder = JSqlClient.newBuilder()
                .setDialect(dialect)
                .setConnectionManager(new SpringConnectionManager(dataSource));
            builder = config(builder);
            client = (JSqlClientImplementor) builder.build();
        }
    }

    protected <R> R execute(Fn<Connection, R> fn) {
        return client.getConnectionManager().execute(connection -> {
            try {
                return fn.apply(connection);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return null;
            }
        });
    }

    public interface Fn<T, R> {
        R apply(T t) throws Exception;
    }

    protected DB using() {
        return DB.H2;
    }

    protected JSqlClient.Builder config(JSqlClient.Builder builder) {
        return builder;
    }

    protected JSqlClientImplementor getClient() {
        init();
        return client;
    }

    protected DataSource newDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        String jdbcUrl = "jdbc:h2:mem:test;MODE\\=%s;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE";
        String mode = switch (using()) {
            case MYSQL, TI_DB -> "MySQL";
            case ORACLE -> "Oracle";
            case SQL_SERVER -> "MSSQLServer";
            case POSTGRESQL -> "PostgreSQL";
            default -> "Regular";
        };
        jdbcUrl = jdbcUrl.formatted(mode);
        dataSource.setJdbcUrl(jdbcUrl);
        return dataSource;
    }

}
