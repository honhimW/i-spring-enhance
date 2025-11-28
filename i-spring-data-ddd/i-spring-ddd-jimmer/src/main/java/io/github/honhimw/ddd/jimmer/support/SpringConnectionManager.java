package io.github.honhimw.ddd.jimmer.support;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class SpringConnectionManager implements DataSourceAwareConnectionManager {

    private final DataSource dataSource;

    public SpringConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public <R> R execute(@Nullable Connection con, Function<Connection, R> block) {
        if (con != null) return block.apply(con);

        Connection newConnection = DataSourceUtils.getConnection(dataSource);
        try {
            return block.apply(newConnection);
        } finally {
            DataSourceUtils.releaseConnection(newConnection, dataSource);
        }
    }
}
